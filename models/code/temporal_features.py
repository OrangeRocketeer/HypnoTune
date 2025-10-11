# # In file: temporal_features.py

import pandas as pd
import numpy as np

def add_temporal_features(epoch_df: pd.DataFrame) -> pd.DataFrame:
    """
    Adds lagged and rolling window features to the epoch DataFrame.

    Args:
        epoch_df: The 30-second epoch DataFrame for a single subject.

    Returns:
        The DataFrame with added temporal features.
    """
    # Define which features to create temporal context for
    features_to_lag = [
        'hr_mean', 'hr_std', 
        'motion_x_std', 'motion_y_std', 'motion_z_std',
        'motion_x_range', 'motion_y_range', 'motion_z_range'
    ]
    
    # Create lagged features for the past 2.5 minutes (5 epochs)
    for feature in features_to_lag:
        for i in range(1, 5):
            epoch_df[f'{feature}_lag_{i}'] = epoch_df[feature].shift(i)

    # Create rolling window features for the past 25 minutes (50 epochs)
    for feature in features_to_lag:
        # min_periods=1 ensures that we get a value even if the window is not full
        rolling_window = epoch_df[feature].rolling(window=50, min_periods=1)
        epoch_df[f'{feature}_rolling_mean_25min'] = rolling_window.mean()
        epoch_df[f'{feature}_rolling_std_25min'] = rolling_window.std()

    # Fill the initial NaNs in rolling_std with 0
    std_cols = [col for col in epoch_df.columns if 'rolling_std' in col]
    epoch_df[std_cols] = epoch_df[std_cols].fillna(0)
    
    return epoch_df

def add_time_since_sleep_onset(epoch_df: pd.DataFrame) -> pd.DataFrame:
    """
    Adds a feature for the time elapsed since the first non-wake epoch.

    Args:
        epoch_df: The 30-second epoch DataFrame for a single subject.

    Returns:
        The DataFrame with the 'time_since_sleep_onset' feature.
    """
    # Find the index of the first epoch of sleep (anything not Wake)
    sleep_indices = epoch_df[epoch_df['sleep_stage'] > 0].index
    
    if not sleep_indices.empty:
        first_sleep_epoch_index = sleep_indices[0]
        
        # Create a counter representing the number of epochs from the start
        epoch_counter = np.arange(len(epoch_df))
        
        # Calculate epochs since sleep onset
        time_since_onset = epoch_counter - epoch_df.index.get_loc(first_sleep_epoch_index)
        
        # Set all pre-sleep values to 0
        time_since_onset = np.maximum(0, time_since_onset)
        
        epoch_df['time_since_sleep_onset'] = time_since_onset
    else:
        # If there is no sleep in the entire recording
        epoch_df['time_since_sleep_onset'] = 0
        
    return epoch_df

from collections import deque

def add_live_temporal_features(epoch_df: pd.DataFrame, buffer: deque) -> pd.DataFrame:
    """
    Temporal features using only available past data for live simulation
    """
    if epoch_df.empty:
        return epoch_df
        
    current_epoch = epoch_df.iloc[0].copy()
    
    # Add available lag features (1-4 lags if available)
    for lag in range(1, 5):
        if len(buffer) >= lag:
            prev_epoch = list(buffer)[-lag]  # Get epoch from lag positions back
            for feature in ['hr_mean', 'hr_std', 'motion_x_std', 'motion_y_std', 'motion_z_std']:
                if feature in prev_epoch:
                    current_epoch[f'{feature}_lag_{lag}'] = prev_epoch[feature]
                else:
                    current_epoch[f'{feature}_lag_{lag}'] = 0
        else:
            # Fill with 0 if not enough history
            for feature in ['hr_mean', 'hr_std', 'motion_x_std', 'motion_y_std', 'motion_z_std']:
                current_epoch[f'{feature}_lag_{lag}'] = 0
    
    # Add rolling features from available past data (simplified)
    if len(buffer) >= 10:
        window_data = list(buffer)[-10:]  # Last 10 epochs (5 minutes)
        for feature in ['hr_mean', 'hr_std']:
            values = [epoch.get(feature, 0) for epoch in window_data if feature in epoch]
            if values:
                current_epoch[f'{feature}_rolling_mean_5min'] = np.mean(values)
                current_epoch[f'{feature}_rolling_std_5min'] = np.std(values) if len(values) > 1 else 0
            else:
                current_epoch[f'{feature}_rolling_mean_5min'] = 0
                current_epoch[f'{feature}_rolling_std_5min'] = 0
    else:
        # Not enough history for rolling features
        for feature in ['hr_mean', 'hr_std']:
            current_epoch[f'{feature}_rolling_mean_5min'] = 0
            current_epoch[f'{feature}_rolling_std_5min'] = 0
    
    return pd.DataFrame([current_epoch])

def add_live_time_since_sleep_onset(epoch_df: pd.DataFrame, buffer: deque) -> pd.DataFrame:
    """
    Estimate time since sleep onset using only past data
    """
    if epoch_df.empty:
        return epoch_df
        
    current_epoch = epoch_df.iloc[0].copy()
    current_sleep_stage = current_epoch.get('sleep_stage', 0)
    
    # Check if sleep onset has occurred in past data
    sleep_onset_detected = False
    sleep_onset_position = 0
    
    # Look through buffer to find if sleep already started
    for i, past_epoch in enumerate(reversed(list(buffer))):
        if past_epoch.get('sleep_stage', 0) > 0:  # Sleep stage found
            sleep_onset_detected = True
            sleep_onset_position = len(buffer) - i
            break
    
    # If current epoch is sleep and no onset detected yet, this is the onset
    if current_sleep_stage > 0 and not sleep_onset_detected:
        sleep_onset_detected = True
        sleep_onset_position = len(buffer)  # Current position
    
    if sleep_onset_detected:
        current_epoch['time_since_sleep_onset'] = len(buffer) - sleep_onset_position
    else:
        current_epoch['time_since_sleep_onset'] = 0
    
    return pd.DataFrame([current_epoch])