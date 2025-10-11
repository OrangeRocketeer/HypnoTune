# In file: model_trainer.py

import pandas as pd
import os
import pickle
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score
import seaborn as sns
import matplotlib.pyplot as plt
from sklearn.utils.class_weight import compute_sample_weight

# Import your previously created modules
from processing_pipeline import process_single_subject
from model_definitions import get_model

def load_and_process_data(folder_path: str) -> pd.DataFrame:
    """
    Loads all CSVs from a folder, processes each one, and concatenates them.
    (This function is reused from the previous response for completeness).
    """
    processed_dfs = []
    all_files = [f for f in os.listdir(folder_path) if f.endswith('.csv')]
    print(f"Found {len(all_files)} files to process in {folder_path}...")
    
    for filename in all_files:
        file_path = os.path.join(folder_path, filename)
        raw_df = pd.read_csv(file_path)
        print(f"  - Processing {filename}...")
        processed_df = process_single_subject(raw_df)
        processed_dfs.append(processed_df)
        
    print("Concatenating all processed subjects...")
    final_dataset = pd.concat(processed_dfs, ignore_index=True)
    return final_dataset

def train_and_evaluate(train_folder: str, test_folder: str, model_name: str, model_save_path: str):
    """
    Orchestrates the full training and evaluation pipeline.

    Args:
        train_folder (str): Path to the folder with training CSVs.
        test_folder (str): Path to the folder with testing CSVs.
        model_name (str): The name of the model to train (e.g., 'xgboost').
        model_save_path (str): Path to save the trained model pickle file.
    """
    # 1. Load and process data
    train_df = load_and_process_data(train_folder)
    test_df = load_and_process_data(test_folder)

    # 2. Separate features (X) and target (y)
    X_train = train_df.drop('sleep_stage', axis=1)
    y_train = train_df['sleep_stage']
    X_test = test_df.drop('sleep_stage', axis=1)
    y_test = test_df['sleep_stage']
    
    
    print("\nCalculating normalization stats from the training set...")
    # Define which features to normalize
    features_to_normalize = [col for col in X_train.columns if 'sleep_stage' not in col]
    
    # Calculate mean and std for each feature across the entire training dataset
    mean_stats = X_train[features_to_normalize].mean()
    std_stats = X_train[features_to_normalize].std()
    
    zero_std_cols = std_stats[std_stats < 1e-6].index.tolist()
    if zero_std_cols:
        print(f"Warning: The following features have zero standard deviation and will be set to 0: {zero_std_cols}")
    
    print("Applying normalization to training and testing sets...")
    
    # Normalize the data safely
    X_train_normalized = (X_train[features_to_normalize] - mean_stats) / (std_stats + 1e-6) # Add epsilon for safety
    X_test_normalized = (X_test[features_to_normalize] - mean_stats) / (std_stats + 1e-6)   # Add epsilon for safety
    
    # For any columns that were constant, the result of normalization could be NaN, so set them to 0.
    if zero_std_cols:
        X_train_normalized[zero_std_cols] = 0
        X_test_normalized[zero_std_cols] = 0

    # Bundle the stats for saving. We must save the column list as well.
    mean_std_stats = {
        'mean': mean_stats,
        'std': std_stats,
        'features': features_to_normalize
    }
    
    print(f"\nTraining data shape: {X_train.shape}")
    print(f"Testing data shape: {X_test.shape}")

    print("\nCalculating class weights to handle imbalance...")
    # The 'balanced' mode automatically adjusts weights inversely proportional to class frequencies.
    # It creates an array where each sample is assigned a weight based on its class.
    sample_weights = compute_sample_weight(
        class_weight='balanced',
        y=y_train
    )
    print("Class weights calculated.")
    
    
    # 3. Get the model
    model = get_model(model_name)

    # 4. Train the model
    print(f"\nTraining the {model_name} model...")
    model.fit(X_train_normalized, y_train, sample_weight=sample_weights)
    print("Training complete.")

    # 5. BUNDLE the model and normalization stats together for saving
    model_and_stats_bundle = {
        'model': model,
        'normalization_stats': mean_std_stats
    }

    print(f"Saving model and stats bundle to {model_save_path}...")
    with open(model_save_path, 'wb') as f:
        pickle.dump(model_and_stats_bundle, f)
    print("Model bundle saved.")

    # 6. Evaluate the model on the test set
    print("\n--- Model Evaluation on Test Set ---")
    y_pred = model.predict(X_test_normalized)
    
    # Define stage labels for reports
    stage_labels = ['Wake', 'Light', 'Deep', 'REM']
    stage_indices = [0, 1, 2, 3]

    print("\nOverall Accuracy:", accuracy_score(y_test, y_pred))
    
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=stage_labels, labels=stage_indices))

    print("\nConfusion Matrix:")
    cm = confusion_matrix(y_test, y_pred, labels=stage_indices)
    # Plotting the confusion matrix
    plt.figure(figsize=(8, 6))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', 
                xticklabels=stage_labels, yticklabels=stage_labels)
    plt.title('Confusion Matrix')
    plt.ylabel('Actual Stage')
    plt.xlabel('Predicted Stage')
    plt.show()

# --- Example Usage ---
if __name__ == '__main__':
    TRAIN_FOLDER = './train_data'
    TEST_FOLDER = './test_data'
    MODEL_NAME = 'xgboost'  # or 'random_forest'
    MODEL_SAVE_PATH = f'./{MODEL_NAME}_sleep_model7.pkl'

    train_and_evaluate(TRAIN_FOLDER, TEST_FOLDER, MODEL_NAME, MODEL_SAVE_PATH)