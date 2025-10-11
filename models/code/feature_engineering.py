# In file: feature_engineering.py

import pandas as pd
import numpy as np

def calculate_rmssd(series):
    """Calculates the RMSSD from a series of heart rate values."""
    # Ensure there are at least two values to calculate a difference
    if len(series) < 2:
        return np.nan
        
    # Calculate successive differences
    successive_diffs = np.diff(series)
    
    # Square the differences, take the mean, then the square root
    rmssd = np.sqrt(np.mean(successive_diffs ** 2))
    
    return rmssd


def create_30s_epochs(df: pd.DataFrame) -> pd.DataFrame:
    """
    Transforms 5-second interval data into 30-second epochs and engineers features.

    Args:
        df: The DataFrame for a single subject with 5-second data.

    Returns:
        A new DataFrame where each row represents one 30-second epoch with
        engineered features.
    """
    # Create an 'epoch_id' to group every 6 rows (30 seconds)
    df['epoch_id'] = np.arange(len(df)) // 6
    
    # Define a function to calculate the range
    def range_func(x):
        return x.max() - x.min()

    # Define the aggregations for each feature
    aggregations = {
        'heart_rate': ['mean', 'std', 'min', 'max', calculate_rmssd],
        'motion_x': ['std', range_func],
        'motion_y': ['std', range_func],
        'motion_z': ['std', range_func],
        'sleep_stage': 'last'  # Take the label from the end of the 30s window
    }

    # Group by the epoch_id and apply the aggregations
    epoch_df = df.groupby('epoch_id').agg(aggregations)

    # Flatten the multi-level column names
    epoch_df.columns = ['_'.join(col).strip() for col in epoch_df.columns.values]
    
    # Rename the columns for clarity
    epoch_df = epoch_df.rename(columns={
        'heart_rate_mean': 'hr_mean',
        'heart_rate_std': 'hr_std',
        'heart_rate_min': 'hr_min',
        'heart_rate_max': 'hr_max',
        'heart_rate_calculate_rmssd': 'hr_rmssd',
        'motion_x_std': 'motion_x_std',
        'motion_y_std': 'motion_y_std',
        'motion_z_std': 'motion_z_std',
        'motion_x_range_func': 'motion_x_range',
        'motion_y_range_func': 'motion_y_range',
        'motion_z_range_func': 'motion_z_range',
        'sleep_stage_last': 'sleep_stage'
    })

    return epoch_df