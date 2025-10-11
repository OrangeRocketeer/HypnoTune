import pandas as pd
import os
import pickle
import json
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
from sklearn.utils.class_weight import compute_sample_weight
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np

from processing_pipeline import process_subject_live_simulation
from model_definitions import get_model

def load_and_process_live_data(folder_path: str) -> pd.DataFrame:
    """
    Loads all CSVs and processes them with live simulation
    """
    processed_dfs = []
    all_files = [f for f in os.listdir(folder_path) if f.endswith('.csv')]
    print(f"Found {len(all_files)} files to process in {folder_path}...")
    
    for filename in all_files:
        file_path = os.path.join(folder_path, filename)
        raw_df = pd.read_csv(file_path)
        print(f"  - Processing {filename} with live simulation...")
        processed_df = process_subject_live_simulation(raw_df)
        processed_dfs.append(processed_df)
        
    print("Concatenating all processed subjects...")
    final_dataset = pd.concat(processed_dfs, ignore_index=True)
    return final_dataset

def convert_lightgbm_to_onnx(model, input_features, onnx_path):
    """
    Convert LightGBM model to ONNX format using onnxmltools
    """
    try:
        import onnxmltools
        from onnxmltools.convert.common.data_types import FloatTensorType
        
        # Define input type with proper shape
        initial_type = [('float_input', FloatTensorType([None, input_features]))]
        
        # Convert to ONNX
        onnx_model = onnxmltools.convert_lightgbm(
            model, 
            initial_types=initial_type,
            target_opset=12  # Use opset 12 for better compatibility
        )
        
        # Save ONNX model
        onnxmltools.utils.save_model(onnx_model, onnx_path)
        print(f"✅ LightGBM model successfully converted to ONNX: {onnx_path}")
        return True
        
    except ImportError:
        print("❌ ERROR: onnxmltools not installed. Install with: pip install onnxmltools")
        return False
    except Exception as e:
        print(f"❌ ERROR: ONNX conversion failed: {str(e)}")
        return False

def verify_onnx_model(onnx_path, X_test_sample):
    """
    Verify the ONNX model can make predictions
    """
    try:
        import onnxruntime as rt
        
        # Load ONNX model
        sess = rt.InferenceSession(onnx_path)
        input_name = sess.get_inputs()[0].name
        label_name = sess.get_outputs()[0].name
        
        # Make prediction with first sample
        test_input = X_test_sample[:1].astype(np.float32)
        pred_onx = sess.run([label_name], {input_name: test_input})[0]
        
        print(f"✅ ONNX model verification successful!")
        print(f"   Sample prediction: {pred_onx[0]}")
        return True
        
    except ImportError:
        print("⚠️  WARNING: onnxruntime not installed. Skipping verification.")
        print("   Install with: pip install onnxruntime")
        return False
    except Exception as e:
        print(f"❌ ERROR: ONNX verification failed: {str(e)}")
        return False

def train_live_model(train_folder: str, test_folder: str, model_name: str, model_save_path: str):
    """
    Train model with live-compatible features and convert to ONNX
    """
    # 1. Load and process data with live simulation
    print("Processing training data with live simulation...")
    train_df = load_and_process_live_data(train_folder)
    
    print("Processing test data with live simulation...")
    test_df = load_and_process_live_data(test_folder)

    # 2. Separate features and target
    X_train = train_df.drop('sleep_stage', axis=1)
    y_train = train_df['sleep_stage']
    X_test = test_df.drop('sleep_stage', axis=1)
    y_test = test_df['sleep_stage']
    
    print(f"\nTraining data shape: {X_train.shape}")
    print(f"Testing data shape: {X_test.shape}")
    print(f"Features: {list(X_train.columns)}")
    
    # 3. Calculate normalization stats
    features_to_normalize = [col for col in X_train.columns if 'sleep_stage' not in col]
    mean_stats = X_train[features_to_normalize].mean()
    std_stats = X_train[features_to_normalize].std()
    
    zero_std_cols = std_stats[std_stats < 1e-6].index.tolist()
    if zero_std_cols:
        print(f"Warning: Zero std features: {zero_std_cols}")
    
    # Normalize
    X_train_normalized = (X_train[features_to_normalize] - mean_stats) / (std_stats + 1e-6)
    X_test_normalized = (X_test[features_to_normalize] - mean_stats) / (std_stats + 1e-6)
    
    if zero_std_cols:
        X_train_normalized[zero_std_cols] = 0
        X_test_normalized[zero_std_cols] = 0
    
    # 4. Calculate sample weights
    sample_weights = compute_sample_weight(class_weight='balanced', y=y_train)
    
    # 5. Get and train model
    model = get_model(model_name)
    print(f"\nTraining {model_name} model with live-compatible features...")
    model.fit(X_train_normalized, y_train, sample_weight=sample_weights)
    print("Training complete!")
    
    # 6. Convert to ONNX
    print("\nStarting ONNX conversion...")
    onnx_path = model_save_path.replace('.pkl', '.onnx')
    num_features = X_train_normalized.shape[1]
    
    onnx_success = convert_lightgbm_to_onnx(model, num_features, onnx_path)
    
    # 7. Verify ONNX model
    if onnx_success:
        print("\nVerifying ONNX model...")
        verify_onnx_model(onnx_path, X_test_normalized.values)
    
    # 8. Save normalization stats as JSON for Android
    stats_path = model_save_path.replace('.pkl', '_stats.json')
    normalization_stats = {
        'mean': mean_stats.to_dict(),
        'std': std_stats.to_dict(),
        'features': features_to_normalize,
        'num_features': len(features_to_normalize),
        'num_classes': 4,
        'class_labels': ['Wake', 'Light', 'Deep', 'REM']
    }
    
    with open(stats_path, 'w') as f:
        json.dump(normalization_stats, f, indent=2)
    print(f"Normalization stats saved to: {stats_path}")
    
    # 9. Also save pickle backup
    model_and_stats_bundle = {
        'model': model,
        'normalization_stats': {
            'mean': mean_stats,
            'std': std_stats,
            'features': features_to_normalize
        }
    }
    
    with open(model_save_path, 'wb') as f:
        pickle.dump(model_and_stats_bundle, f)
    print(f"Pickle backup saved to {model_save_path}")
    
    # 10. Evaluate
    print("\n--- Evaluation on Test Set ---")
    y_pred = model.predict(X_test_normalized)
    
    stage_labels = ['Wake', 'Light', 'Deep', 'REM']
    stage_indices = [0, 1, 2, 3]
    
    print("\nOverall Accuracy:", accuracy_score(y_test, y_pred))
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=stage_labels, labels=stage_indices))
    
    # Plot confusion matrix
    cm = confusion_matrix(y_test, y_pred, labels=stage_indices)
    cm_normalized = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
    cm_normalized = np.nan_to_num(cm_normalized)

    plt.figure(figsize=(8, 6))
    sns.heatmap(cm_normalized, annot=True, fmt='.2f', cmap='Blues', 
                xticklabels=stage_labels, yticklabels=stage_labels)
    plt.title('Confusion Matrix - Live Compatible Model')
    plt.ylabel('Actual Stage')
    plt.xlabel('Predicted Stage')
    plt.show()
    
    # 11. Print Android integration info
    print("\n" + "="*50)
    print("ANDROID INTEGRATION INFO:")
    print("="*50)
    if onnx_success:
        print(f"✅ ONNX Model: {onnx_path}")
        print(f"✅ Stats File: {stats_path}")
        print(f"✅ Number of input features: {num_features}")
        print(f"✅ Model is ready for Android integration!")
        print("\nAdd to your Android app:")
        print("  implementation 'com.microsoft.onnxruntime:onnxruntime-android:latest.release'")
    else:
        print("❌ ONNX conversion failed. Using pickle backup.")
        print(f"   Pickle file: {model_save_path}")
    print("="*50)

if __name__ == '__main__':
    TRAIN_FOLDER = './train_data'
    TEST_FOLDER = './test_data'
    MODEL_NAME = 'lightgbm'
    MODEL_SAVE_PATH = f'./{MODEL_NAME}_live_model3.pkl'
    
    train_live_model(TRAIN_FOLDER, TEST_FOLDER, MODEL_NAME, MODEL_SAVE_PATH)