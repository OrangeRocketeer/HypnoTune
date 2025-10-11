# In file: model_definitions.py

from sklearn.ensemble import RandomForestClassifier
from xgboost import XGBClassifier
try:
    import lightgbm as lgb
    LIGHTGBM_AVAILABLE = True
except ImportError:
    LIGHTGBM_AVAILABLE = False
    print("LightGBM not installed. Install with: pip install lightgbm")
# To use DL models, you would need libraries like TensorFlow/Keras or PyTorch
# from tensorflow.keras.models import Sequential
# from tensorflow.keras.layers import Dense, LSTM

def get_model(model_name: str, random_state: int = 42):
    """
    Returns an untrained model instance based on the provided name.
    
    Args:
        model_name (str): The name of the model. 
                          Supported: 'random_forest', 'xgboost'.
        random_state (int): A random seed for reproducibility.
                          
    Returns:
        An untrained classifier object with a scikit-learn compatible API 
        (i.e., it has .fit() and .predict() methods).
    """
    if model_name.lower() == 'random_forest':
        print("Initializing RandomForestClassifier.")
        # n_jobs=-1 uses all available CPU cores
        return RandomForestClassifier(n_estimators=300,
                                      min_samples_leaf=5,
                                      min_samples_split=5,
                                      max_depth=8,
                                      random_state=random_state,
                                      n_jobs=-1)
        
    elif model_name.lower() == 'xgboost':
        print("Initializing XGBClassifier.")
        # These are basic parameters; XGBoost has many for tuning.
        return XGBClassifier(
            objective='multi:softmax', 
            num_class=4,
            use_label_encoder=False, 
            eval_metric='mlogloss',
            random_state=random_state, 
            n_jobs=-1,
            
            # --- Tuned Hyperparameters ---
            learning_rate=0.1,  # Slower learning
            n_estimators=200,   # More trees to compensate for slower learning
            max_depth=8,        # Allow more complex trees
            subsample=0.8,      # Use 80% of data for each tree (prevents overfitting)
            colsample_bytree=0.8# Use 80% of features for each tree (prevents overfitting)
        )
        
    elif model_name.lower() == 'lightgbm':
        if not LIGHTGBM_AVAILABLE:
            raise ImportError("LightGBM is not installed. Please install with: pip install lightgbm")
        
        print("Initializing LGBMClassifier.")
        return lgb.LGBMClassifier(
            objective='multiclass',
            num_class=4,
            random_state=random_state,
            n_jobs=-1,
            
            # LightGBM specific parameters (similar performance to your XGBoost setup)
            learning_rate=0.1,
            n_estimators=200,
            max_depth=8,        # Allow more complex trees
            subsample=0.8,      # Use 80% of data for each tree (prevents overfitting)
            colsample_bytree=0.8,
            # min_gain_to_split=0.03,
            
            # LightGBM specific optimizations
            reg_alpha=0.1,      # L1 regularization
            reg_lambda=0.1,     # L2 regularization
            verbosity=-1        # Silent mode
        )
    else:
        raise ValueError(f"Model '{model_name}' is not supported. Choose from 'random_forest', 'xgboost'.")