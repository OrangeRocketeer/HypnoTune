# In file: inference.py

import pandas as pd
import os
import pickle
import matplotlib.pyplot as plt
from sklearn.metrics import classification_report, accuracy_score

# Import the main processing function
from processing_pipeline import process_single_subject

def run_inference_on_folder(test_folder: str, model_path: str):
    """
    Loads a trained model and runs inference on all CSV files in a folder.
    For each file, it prints metrics and generates a comparison plot.

    Args:
        test_folder (str): Path to the folder with test CSVs.
        model_path (str): Path to the saved .pkl model file.
    """
    # 1. Load the trained model
    print(f"Loading model and stats bundle from {model_path}...")
    with open(model_path, 'rb') as f:
        bundle = pickle.load(f)
    
    model = bundle['model']
    norm_stats = bundle['normalization_stats']
    print("Model and stats loaded successfully.")

    # 2. Get list of files to process
    all_files = [f for f in os.listdir(test_folder) if f.endswith('.csv')]
    
    if not all_files:
        print(f"No CSV files found in {test_folder}.")
        return

    stage_labels = ['Wake', 'Light', 'Deep', 'REM']
    stage_indices = [0, 1, 2, 3]

    # 3. Process each file individually
    for filename in all_files:
        print(f"\n--- Processing file: {filename} ---")
        file_path = os.path.join(test_folder, filename)
        
        # a. Load and process the single subject's data
        raw_df = pd.read_csv(file_path)
        processed_df = process_single_subject(raw_df)
        
        if processed_df.empty:
            print(f"Not enough data in {filename} to make a prediction after processing. Skipping.")
            continue
            
        # b. Separate features and true labels
        # Ensure the subject's columns match the training columns exactly
        X_subject = processed_df.drop('sleep_stage', axis=1)
        X_subject = X_subject.reindex(columns=norm_stats['features'], fill_value=0)
        y_true = processed_df['sleep_stage']
        
        # Normalize the new data using the loaded population stats safely
        mean_stats = norm_stats['mean']
        std_stats = norm_stats['std']

        # c. Predict sleep stages
        # Safely normalize the data
        X_subject_normalized = (X_subject - mean_stats) / (std_stats + 1e-6) # Add epsilon
         # Handle columns that had zero std in the training set
        zero_std_cols = std_stats[std_stats < 1e-6].index.tolist()
        if zero_std_cols:
            X_subject_normalized[zero_std_cols] = 0
            
        y_pred = model.predict(X_subject_normalized)

        # d. Report metrics for this specific file
        print(f"\nMetrics for {filename}:")
        print("Overall Accuracy:", accuracy_score(y_true, y_pred))
        print("\nClassification Report:")
        # Use zero_division=0 to prevent warnings if a class is not present in y_true
        print(classification_report(y_true, y_pred, target_names=stage_labels, 
                                    labels=stage_indices, zero_division=0))
        
        # e. Plot the results (Hypnogram)
        fig, ax = plt.subplots(figsize=(15, 5))
        
        # Plot actual vs. predicted
        ax.plot(y_true.index, y_true, label='Actual Stage', color='blue', alpha=0.7, drawstyle='steps-post')
        ax.plot(y_pred, label='Predicted Stage', color='red', alpha=0.6, linestyle='--', drawstyle='steps-post')

        ax.set_yticks(stage_indices)
        ax.set_yticklabels(stage_labels)
        ax.set_xlabel('Epoch (30-second intervals)')
        ax.set_ylabel('Sleep Stage')
        ax.set_title(f'Sleep Stage Prediction for {filename}')
        ax.legend()
        plt.gca().invert_yaxis() # Standard for hypnograms (deeper sleep is lower)
        plt.tight_layout()
        plt.show()

# --- Example Usage ---
if __name__ == '__main__':
    TEST_INFERENCE_FOLDER = './exp-csv'  # Or a new folder with unseen data
    MODEL_PATH = './xgboost_sleep_model7.pkl' # Path to the model saved by model_trainer.py

    run_inference_on_folder(TEST_INFERENCE_FOLDER, MODEL_PATH)