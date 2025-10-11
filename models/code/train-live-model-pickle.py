import pandas as pd
import os
import pickle
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

def train_live_model(train_folder: str, test_folder: str, model_name: str, model_save_path: str):
    """
    Train model with live-compatible features
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
    
    # 6. Save model bundle
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
    print(f"Model saved to {model_save_path}")
    
    # 7. Evaluate
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

if __name__ == '__main__':
    TRAIN_FOLDER = './train_data'
    TEST_FOLDER = './test_data'
    MODEL_NAME = 'lightgbm'
    MODEL_SAVE_PATH = f'./{MODEL_NAME}_live_model3.pkl'
    
    train_live_model(TRAIN_FOLDER, TEST_FOLDER, MODEL_NAME, MODEL_SAVE_PATH)