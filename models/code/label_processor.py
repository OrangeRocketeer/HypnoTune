# In file: label_processor.py

import pandas as pd

def remap_sleep_stages(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans and remaps the sleep_stage column to a 4-stage classification.

    - Wake: 0
    - Light Sleep (N1 + N2): 1
    - Deep Sleep (N3): 2
    - REM: 3

    Also handles artifacts like NaN, -1, and 4.

    Args:
        df: The raw DataFrame for a single subject with a 'sleep_stage' column.

    Returns:
        The DataFrame with the 'sleep_stage' column remapped.
    """
    # Create a mapping dictionary for the stages
    stage_mapping = {
        -1: 0,  # Artifact -> Wake
        0: 0,   # Wake -> Wake
        1: 1,   # N1 -> Light
        2: 1,   # N2 -> Light
        3: 2,   # N3 -> Deep
        4: 3,   # Artifact -> REM
        5: 3    # REM -> REM (assuming 5 is the original REM label)
    }

    # Apply the mapping
    df['sleep_stage'] = df['sleep_stage'].map(stage_mapping)
    
    # Forward-fill any remaining NaNs, treating them as the previous stage
    df['sleep_stage'] = df['sleep_stage'].ffill()

    # If there are still NaNs at the beginning, back-fill them
    df['sleep_stage'] = df['sleep_stage'].bfill()

    # Ensure the column is of integer type
    df['sleep_stage'] = df['sleep_stage'].astype(int)

    return df