# In file: processing_pipeline.py

import pandas as pd
from collections import deque

# Import the functions from your other modules
from label_processor import remap_sleep_stages
from feature_engineering import create_30s_epochs
from temporal_features import add_temporal_features, add_time_since_sleep_onset
from temporal_features import add_live_temporal_features, add_live_time_since_sleep_onset

def process_single_subject(df: pd.DataFrame) -> pd.DataFrame:
    """
    Runs a single subject's raw DataFrame through the entire processing pipeline.

    Args:
        df: The raw 5-second interval DataFrame for one subject.

    Returns:
        A fully processed DataFrame for the subject, ready for model training.
    """
    # Step 3: Remap sleep stage labels
    df_labeled = remap_sleep_stages(df)
    
    # Step 4: Create 30-second epochs with engineered features
    df_epochs = create_30s_epochs(df_labeled)
    
    # Step 5: Add lagged and rolling window features
    df_temporal = add_temporal_features(df_epochs)
    
    # Step 6: Add time since sleep onset feature
    df_with_onset = add_time_since_sleep_onset(df_temporal)
    
    # Step 7: Final cleanup - drop rows with NaNs from lagging
    df_clean = df_with_onset.dropna().reset_index(drop=True)
    
    return df_clean

def process_subject_live_simulation(raw_df: pd.DataFrame) -> pd.DataFrame:
    """
    Processes data to simulate live conditions - only uses past data for features
    """
    # Step 1: Remap sleep stages
    df_labeled = remap_sleep_stages(raw_df)
    
    # Step 2: Create 30-second epochs
    df_epochs = create_30s_epochs(df_labeled)
    
    # Step 3: Process each epoch with only past data available
    live_processed_epochs = []
    previous_epochs_buffer = deque(maxlen=50)  # Keep last 50 epochs
    
    for i in range(len(df_epochs)):
        current_epoch = df_epochs.iloc[i:i+1].copy()
        
        # Add live temporal features (only past data)
        epoch_with_temporal = add_live_temporal_features(current_epoch, previous_epochs_buffer)
        
        # Add live time since sleep onset
        epoch_with_onset = add_live_time_since_sleep_onset(epoch_with_temporal, previous_epochs_buffer)
        
        # Add to results
        live_processed_epochs.append(epoch_with_onset)
        
        # Add current epoch to buffer for next iteration (AFTER processing)
        if not epoch_with_onset.empty:
            previous_epochs_buffer.append(epoch_with_onset.iloc[0].to_dict())
    
    # Combine all processed epochs
    if live_processed_epochs:
        final_df = pd.concat(live_processed_epochs, ignore_index=True)
        return final_df.dropna().reset_index(drop=True)
    else:
        return pd.DataFrame()