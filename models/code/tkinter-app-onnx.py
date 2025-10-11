# live_sleep_predictor.py

import tkinter as tk
from tkinter import ttk, filedialog, messagebox
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
import numpy as np
from collections import deque
import threading
import time
import traceback

# Import your processing functions
from processing_pipeline import process_single_subject
from processing_pipeline import process_subject_live_simulation
from feature_engineering import create_30s_epochs
from temporal_features import add_live_temporal_features, add_live_time_since_sleep_onset
from label_processor import remap_sleep_stages

try:
    import onnxruntime as ort
except ImportError:
    print("ONNX Runtime not installed. Please install with: pip install onnxruntime")


class LiveSleepPredictor:
    def __init__(self, root):
        self.root = root
        self.root.title("Live Sleep Stage Predictor")
        self.root.geometry("1200x800")
        
        # Initialize variables
        self.model = None
        self.norm_stats = None
        self.raw_data = None
        self.processed_buffer = None
        self.sleep_onset_epoch = None
        self.current_index = 0
        self.prediction_history = deque()  # Keep last 100 predictions
        self.time_history = deque()
        
        # Data buffer for accumulating 5-second samples
        self.data_buffer = []
        self.epoch_counter = 0
        
        self.setup_ui()
        
    def setup_ui(self):
        # Main frame
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Control panel
        control_frame = ttk.LabelFrame(main_frame, text="Controls", padding="10")
        control_frame.grid(row=0, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        # Model loading
        ttk.Button(control_frame, text="Load Model", 
                  command=self.load_model).grid(row=0, column=0, padx=(0, 10))
        self.model_label = ttk.Label(control_frame, text="No model loaded")
        self.model_label.grid(row=0, column=1, padx=(0, 20))
        
        # Data loading
        ttk.Button(control_frame, text="Load CSV Data", 
                  command=self.load_csv_data).grid(row=0, column=2, padx=(0, 10))
        self.data_label = ttk.Label(control_frame, text="No data loaded")
        self.data_label.grid(row=0, column=3, padx=(0, 20))
        
        # Simulation controls
        ttk.Button(control_frame, text="Next Sample", 
                  command=self.process_next_sample).grid(row=0, column=4, padx=(0, 10))
        ttk.Button(control_frame, text="Auto Play (1x)", 
                  command=self.start_auto_play).grid(row=0, column=5, padx=(0, 10))
        ttk.Button(control_frame, text="Auto Play (50x)", 
                  command=lambda: self.start_auto_play(0.02)).grid(row=0, column=6, padx=(0, 10))
        ttk.Button(control_frame, text="Stop", 
                  command=self.stop_auto_play).grid(row=0, column=7, padx=(0, 10))
        ttk.Button(control_frame, text="Reset", 
                  command=self.reset_simulation).grid(row=0, column=8)
        
        # Status panel
        status_frame = ttk.LabelFrame(main_frame, text="Current Status", padding="10")
        status_frame.grid(row=1, column=0, columnspan=2, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.status_text = tk.Text(status_frame, height=8, width=80)
        self.status_text.grid(row=0, column=0, sticky=(tk.W, tk.E))
        status_scrollbar = ttk.Scrollbar(status_frame, orient="vertical", command=self.status_text.yview)
        status_scrollbar.grid(row=0, column=1, sticky=(tk.N, tk.S))
        self.status_text.configure(yscrollcommand=status_scrollbar.set)
        
        # Prediction display
        pred_frame = ttk.LabelFrame(main_frame, text="Current Prediction", padding="10")
        pred_frame.grid(row=2, column=0, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.prediction_label = ttk.Label(pred_frame, text="No prediction yet", 
                                         font=("Arial", 16, "bold"))
        self.prediction_label.grid(row=0, column=0)
        
        self.confidence_label = ttk.Label(pred_frame, text="", font=("Arial", 12))
        self.confidence_label.grid(row=1, column=0)
        
        # Feature display
        feature_frame = ttk.LabelFrame(main_frame, text="Current Features", padding="10")
        feature_frame.grid(row=2, column=1, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.feature_text = tk.Text(feature_frame, height=8, width=40)
        self.feature_text.grid(row=0, column=0, sticky=(tk.W, tk.E))
        
        # Plot frame
        plot_frame = ttk.LabelFrame(main_frame, text="Live Prediction History", padding="10")
        plot_frame.grid(row=3, column=0, columnspan=2, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Create matplotlib figure
        self.fig, self.ax = plt.subplots(figsize=(15, 4))
        self.canvas = FigureCanvasTkAgg(self.fig, master=plot_frame)
        self.canvas.get_tk_widget().grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Configure grid weights for resizing
        main_frame.columnconfigure(0, weight=1)
        main_frame.columnconfigure(1, weight=1)
        main_frame.rowconfigure(3, weight=1)
        plot_frame.columnconfigure(0, weight=1)
        plot_frame.rowconfigure(0, weight=1)
        
        self.auto_play = False
        self.auto_play_speed = 1.0
        
    def load_model(self):
        """Load the trained ONNX model and normalization stats"""
        # First, load the ONNX model file
        onnx_path = filedialog.askopenfilename(
            title="Select ONNX Model File",
            filetypes=[("ONNX files", "*.onnx"), ("All files", "*.*")]
        )
        
        if not onnx_path:
            return
        
        # Then, load the corresponding stats JSON file
        stats_path = filedialog.askopenfilename(
            title="Select Stats JSON File",
            filetypes=[("JSON files", "*.json"), ("All files", "*.*")]
        )
        
        if not stats_path:
            messagebox.showwarning("Warning", "Please select both ONNX model and stats JSON file")
            return
        
        try:
            import onnxruntime as ort
            import json
            
            # Load ONNX model
            self.session = ort.InferenceSession(onnx_path)
            self.model = self.session  # For compatibility with existing code
            
            # Load normalization stats
            with open(stats_path, 'r') as f:
                stats_data = json.load(f)
            
            # FIXED: Handle array-based JSON structure
            # Create pandas Series by pairing features with their values
            features_list = stats_data['features']
            mean_values = stats_data['mean']
            std_values = stats_data['std']
            
            # Create Series with features as index
            self.norm_stats = {
                'mean': pd.Series(data=mean_values, index=features_list),
                'std': pd.Series(data=std_values, index=features_list),
                'features': features_list
            }
            
            # Verify the stats are loaded correctly
            print("Mean stats sample:")
            print(self.norm_stats['mean'].head())
            print(f"Mean stats type: {type(self.norm_stats['mean'].iloc[0])}")
            
            # Get model info
            input_info = self.session.get_inputs()[0]
            output_info = self.session.get_outputs()[0]
            
            self.model_label.config(text=f"ONNX Model loaded")
            self.log_status(f"✓ ONNX model loaded successfully from {onnx_path}")
            self.log_status(f"  - Features: {len(self.norm_stats['features'])}")
            self.log_status(f"  - Input: {input_info.name}, shape: {input_info.shape}")
            self.log_status(f"  - Output: {output_info.name}, shape: {output_info.shape}")
            self.log_status(f"  - Mean stats loaded: hr_mean={self.norm_stats['mean']['hr_mean']:.2f}")
            
            # Test normalization on dummy data
            test_df = pd.DataFrame([[0] * len(features_list)], columns=features_list)
            try:
                test_norm = (test_df - self.norm_stats['mean']) / (self.norm_stats['std'] + 1e-6)
                self.log_status(f"✓ Normalization test passed")
            except Exception as e:
                self.log_status(f"❌ Normalization test failed: {str(e)}")
            
        except ImportError:
            messagebox.showerror("Error", "ONNX Runtime not installed. Install with: pip install onnxruntime")
        except Exception as e:
            import traceback
            messagebox.showerror("Error", f"Failed to load ONNX model: {str(e)}\n\n{traceback.format_exc()}")

    def precompute_actual_sleep_stages(self, raw_df):
        """Precompute actual sleep stages from the entire CSV file"""
        try:
            # Process the entire file to get actual sleep stages
            processed_df = process_single_subject(raw_df)
            return processed_df['sleep_stage'].tolist()
        except Exception as e:
            self.log_status(f"Error precomputing actual stages: {str(e)}")
            return []
    
    def load_csv_data(self):
        """Load CSV data for simulation"""
        file_path = filedialog.askopenfilename(
            title="Select CSV Data File",
            filetypes=[("CSV files", "*.csv"), ("All files", "*.*")]
        )
        
        if file_path:
            try:
                self.raw_data = pd.read_csv(file_path)
                self.current_index = 0
                self.data_buffer = []
                self.epoch_counter = 0
                self.prediction_history.clear()
                self.time_history.clear()
                
                self.actual_sleep_stages = self.precompute_actual_sleep_stages(self.raw_data)
                self.actual_times = [i * 30 for i in range(len(self.actual_sleep_stages))]
                
                self.data_label.config(text=f"Data loaded: {len(self.raw_data)} rows")
                self.log_status(f"✓ CSV data loaded: {len(self.raw_data)} rows")
                self.log_status(f"  - Columns: {list(self.raw_data.columns)}")
                self.log_status(f"  - First timestamp: {self.raw_data['timestamp'].iloc[0]}")
                
                # Initialize plot
                self.update_plot()
                
            except Exception as e:
                messagebox.showerror("Error", f"Failed to load CSV: {str(e)}")
    
    def process_next_sample(self):
        """Process the next 5-second sample and make prediction when 30s epoch is complete"""
        if self.model is None or self.raw_data is None:
            messagebox.showwarning("Warning", "Please load both model and data first")
            return
        
        if self.current_index >= len(self.raw_data):
            self.log_status("✓ End of data reached")
            return
        
        # Get current 5-second sample
        current_sample = self.raw_data.iloc[self.current_index].copy()
        self.data_buffer.append(current_sample)
        
        self.log_status(f"Processing sample {self.current_index + 1}: "
                       f"HR={current_sample['heart_rate']}, "
                       f"Motion=({current_sample['motion_x']:.2f}, "
                       f"{current_sample['motion_y']:.2f}, "
                       f"{current_sample['motion_z']:.2f})")
        
        # Check if we have a complete 30-second epoch (6 samples)
        if len(self.data_buffer) == 6:
            prediction, confidence, features = self.process_epoch()
            if prediction is not None:
                self.display_prediction(prediction, confidence, features)
                self.update_plot()
            
            # Clear buffer for next epoch
            self.data_buffer = []
            self.epoch_counter += 1
        
        self.current_index += 1
        
        # Update progress
        progress = (self.current_index / len(self.raw_data)) * 100
        self.root.title(f"Live Sleep Stage Predictor - Progress: {progress:.1f}%")
    
    def process_epoch(self):
        """Process a complete 30-second epoch and make prediction"""
        try:
            # Create DataFrame from buffer
            epoch_df = pd.DataFrame(self.data_buffer)
            
            # Apply the same processing pipeline as during training
            processed_epoch = self.process_single_epoch(epoch_df)
            
            if processed_epoch is None or processed_epoch.empty:
                self.log_status("Warning: Processing returned empty result")
                return None, None, None
            
            # Ensure it's a DataFrame
            if isinstance(processed_epoch, dict):
                processed_epoch = pd.DataFrame([processed_epoch])
            
            # Log the shape and columns for debugging
            self.log_status(f"Processed epoch shape: {processed_epoch.shape}, columns: {len(processed_epoch.columns)}")
            
            # Prepare features for prediction
            X_epoch = processed_epoch.drop('sleep_stage', axis=1, errors='ignore')
            
            # Log features before normalization
            self.log_status(f"Features before normalization: {list(X_epoch.columns)[:5]}...")
            
            # Reindex to match training features, fill missing with 0
            X_epoch = X_epoch.reindex(columns=self.norm_stats['features'], fill_value=0)
            
            # Normalize using saved statistics
            mean_stats = self.norm_stats['mean']
            std_stats = self.norm_stats['std']
            
            # Simple vectorized normalization
            X_epoch_normalized = (X_epoch - mean_stats) / (std_stats + 1e-6)
            
            # Handle zero-std columns
            zero_std_mask = std_stats < 1e-6
            if zero_std_mask.any():
                X_epoch_normalized.loc[:, zero_std_mask] = 0
            
            # Make prediction with ONNX model
            if hasattr(self, 'session'):
                # ONNX model inference
                input_name = self.session.get_inputs()[0].name
                input_data = X_epoch_normalized.astype(np.float32).values.reshape(1, -1)
                
                self.log_status(f"Input shape for ONNX: {input_data.shape}")
                
                outputs = self.session.run(None, {input_name: input_data})
                
                # Debug: Print output structure
                self.log_status(f"Number of outputs: {len(outputs)}")
                for i, output in enumerate(outputs):
                    self.log_status(f"Output {i}: type={type(output)}, shape={getattr(output, 'shape', 'no shape')}")
                    # If it's a list, show what's inside
                    if isinstance(output, list):
                        self.log_status(f"  List length: {len(output)}, first element type: {type(output[0]) if output else 'empty'}")
                        if output and isinstance(output[0], dict):
                            self.log_status(f"  Dict keys: {output[0].keys()}")
                
                try:
                    # Get prediction label
                    if isinstance(outputs[0], np.ndarray):
                        prediction = int(outputs[0][0]) if outputs[0].size > 0 else int(outputs[0])
                    else:
                        prediction = int(outputs[0])
                    
                    # Get confidence/probabilities from output[1]
                    if len(outputs) > 1:
                        prob_output = outputs[1]
                        
                        # Handle list of dicts (LightGBM ONNX format)
                        if isinstance(prob_output, list):
                            self.log_status(f"Probability output is list with {len(prob_output)} elements")
                            
                            if len(prob_output) > 0:
                                first_elem = prob_output[0]
                                
                                # If it's a dict with class probabilities
                                if isinstance(first_elem, dict):
                                    self.log_status(f"First element is dict with keys: {first_elem.keys()}")
                                    
                                    # LightGBM often outputs like: [{0: 0.1, 1: 0.3, 2: 0.4, 3: 0.2}]
                                    # Extract probability values
                                    prob_values = list(first_elem.values())
                                    probabilities = np.array(prob_values)
                                    confidence = float(np.max(probabilities))
                                    
                                # If it's already a number/array
                                elif isinstance(first_elem, (int, float, np.number)):
                                    probabilities = np.array(prob_output)
                                    confidence = float(np.max(probabilities))
                                
                                else:
                                    self.log_status(f"Unknown list element type: {type(first_elem)}")
                                    confidence = 1.0
                            else:
                                confidence = 1.0
                        
                        # Handle dict directly
                        elif isinstance(prob_output, dict):
                            self.log_status(f"Probability output is dict with keys: {prob_output.keys()}")
                            prob_values = list(prob_output.values())
                            probabilities = np.array(prob_values)
                            confidence = float(np.max(probabilities))
                        
                        # Handle array
                        elif isinstance(prob_output, np.ndarray):
                            if prob_output.ndim > 1:
                                probabilities = prob_output[0]
                            else:
                                probabilities = prob_output
                            confidence = float(np.max(probabilities))
                        
                        else:
                            self.log_status(f"Unknown probability output type: {type(prob_output)}")
                            confidence = 1.0
                    else:
                        # Only one output (just predictions)
                        confidence = 1.0
                    
                    self.log_status(f"✓ Prediction: {prediction}, Confidence: {confidence:.2%}")
                    
                except Exception as pred_error:
                    import traceback
                    self.log_status(f"Error extracting prediction: {str(pred_error)}")
                    self.log_status(traceback.format_exc())
                    return None, None, None
                    
            else:
                # Fallback to scikit-learn model
                prediction = self.model.predict(X_epoch_normalized)[0]
                if hasattr(self.model, 'predict_proba'):
                    probabilities = self.model.predict_proba(X_epoch_normalized)[0]
                    confidence = max(probabilities)
                else:
                    confidence = 1.0

            # Store prediction for plotting
            self.prediction_history.append(prediction)
            self.time_history.append(self.epoch_counter)

            return prediction, confidence, X_epoch.iloc[0]
        except Exception as pred_error:
            import traceback
            self.log_status(f"Error extracting prediction: {str(pred_error)}")
            self.log_status(traceback.format_exc())
            return None, None, None
    
    def process_single_epoch(self, epoch_df):
        """Process a single 30-second epoch for live prediction"""
        try:
            # Step 1: Remap sleep stages
            df_labeled = remap_sleep_stages(epoch_df)
            
            # Step 2: Create 30s epoch features
            df_epochs = self.create_single_epoch_features(df_labeled)
            
            # Make sure it's a DataFrame
            if not isinstance(df_epochs, pd.DataFrame):
                self.log_status(f"Error: create_single_epoch_features returned {type(df_epochs)}")
                return None
            
            # Initialize buffer if not exists
            if not hasattr(self, 'previous_epochs_buffer'):
                self.previous_epochs_buffer = deque(maxlen=50)
            
            # Step 3: Add live temporal features
            df_temporal = add_live_temporal_features(df_epochs, self.previous_epochs_buffer)
            
            # Verify it's still a DataFrame
            if not isinstance(df_temporal, pd.DataFrame):
                self.log_status(f"Error: add_live_temporal_features returned {type(df_temporal)}")
                # Convert back to DataFrame if it's a dict
                if isinstance(df_temporal, dict):
                    df_temporal = pd.DataFrame([df_temporal])
            
            # Step 4: Add live time since sleep onset
            df_with_onset = add_live_time_since_sleep_onset(df_temporal, self.previous_epochs_buffer)
            
            # Final verification
            if not isinstance(df_with_onset, pd.DataFrame):
                self.log_status(f"Error: add_live_time_since_sleep_onset returned {type(df_with_onset)}")
                if isinstance(df_with_onset, dict):
                    df_with_onset = pd.DataFrame([df_with_onset])
            
            # Add to buffer for next iteration
            if not df_with_onset.empty:
                self.previous_epochs_buffer.append(df_with_onset.iloc[0].to_dict())
            
            return df_with_onset
            
        except Exception as e:
            import traceback
            self.log_status(f"Error in processing pipeline: {str(e)}")
            self.log_status(traceback.format_exc())
            return None
    
    def create_single_epoch_features(self, df):
        """Create features for a single 30-second epoch"""
        # Calculate features manually for the single epoch
        features = {}
        
        # Heart rate features
        hr_series = df['heart_rate']
        features['hr_mean'] = hr_series.mean()
        features['hr_std'] = hr_series.std()
        features['hr_min'] = hr_series.min()
        features['hr_max'] = hr_series.max()
        features['hr_rmssd'] = self.calculate_rmssd(hr_series)
        
        # Motion features
        for axis in ['x', 'y', 'z']:
            motion_series = df[f'motion_{axis}']
            features[f'motion_{axis}_std'] = motion_series.std()
            features[f'motion_{axis}_range'] = motion_series.max() - motion_series.min()
        
        # Sleep stage (use the last value)
        features['sleep_stage'] = df['sleep_stage'].iloc[-1]
        
        return pd.DataFrame([features])
    
    def calculate_rmssd(self, series):
        """Calculate RMSSD for heart rate variability"""
        if len(series) < 2:
            return 0
        successive_diffs = np.diff(series)
        return np.sqrt(np.mean(successive_diffs ** 2))
    
    def add_simplified_temporal_features(self, df_epoch):
        current_features = df_epoch.iloc[0].copy()
        
        # Add more lag features if we have previous data
        if hasattr(self, 'previous_epochs') and len(self.previous_epochs) > 0:
            # Add lags 1-4 if available
            for lag in range(1, 5):
                if len(self.previous_epochs) >= lag:
                    prev_epoch = self.previous_epochs[-lag]
                    for feature in ['hr_mean', 'hr_std', 'motion_x_std', 'motion_y_std', 'motion_z_std']:
                        if feature in prev_epoch:
                            current_features[f'{feature}_lag_{lag}'] = prev_epoch[feature]
        
        # Add simple rolling features (simplified)
        if hasattr(self, 'previous_epochs') and len(self.previous_epochs) > 10:
            recent_hr = [epoch.get('hr_mean', 0) for epoch in list(self.previous_epochs)[-10:]]
            current_features['hr_mean_rolling_mean_25min'] = np.mean(recent_hr)
            current_features['hr_mean_rolling_std_25min'] = np.std(recent_hr)
        
        return pd.DataFrame([current_features])

    def add_simplified_time_onset(self, df_epoch):
        current_features = df_epoch.iloc[0].copy()
        
        # Simple sleep onset detection
        if not hasattr(self, 'sleep_onset_epoch'):
            self.sleep_onset_epoch = None
        
        # If current epoch is sleep (not wake) and we haven't detected onset yet
        if current_features.get('sleep_stage', 0) > 0 and self.sleep_onset_epoch is None:
            self.sleep_onset_epoch = self.epoch_counter
        
        if self.sleep_onset_epoch is not None:
            current_features['time_since_sleep_onset'] = self.epoch_counter - self.sleep_onset_epoch
        else:
            current_features['time_since_sleep_onset'] = 0
            
        return pd.DataFrame([current_features])
    
    def display_prediction(self, prediction, confidence, features):
        """Display the current prediction and features"""
        stage_names = {0: 'WAKE', 1: 'LIGHT', 2: 'DEEP', 3: 'REM'}
        stage_colors = {0: 'red', 1: 'darkorange', 2: 'blue', 3: 'green'}
        
        stage_name = stage_names.get(prediction, 'UNKNOWN')
        color = stage_colors.get(prediction, 'gray')
        
        self.prediction_label.config(
            text=f"Predicted Stage: {stage_name}",
            foreground=color
        )
        
        self.confidence_label.config(
            text=f"Confidence: {confidence:.2%}",
            foreground=color
        )
        
        # Display important features
        if features is not None:
            feature_text = "Current Features:\n"
            important_features = {
                'hr_mean': 'Heart Rate Mean',
                'hr_std': 'HR Variability',
                'hr_rmssd': 'HR RMSSD',
                'motion_x_std': 'Motion X STD'
            }
            
            for feat_key, feat_name in important_features.items():
                if feat_key in features:
                    value = features[feat_key]
                    feature_text += f"{feat_name}: {value:.2f}\n"
            
            self.feature_text.delete(1.0, tk.END)
            self.feature_text.insert(1.0, feature_text)
        
        self.log_status(f"✓ Epoch {self.epoch_counter}: Predicted {stage_name} "
                       f"(confidence: {confidence:.2%})")
    
    # def update_plot(self):
    #     """Update the live prediction plot"""
    #     self.ax.clear()
        
    #     if self.prediction_history:
    #         # Create colormap for sleep stages
    #         colors = [['red', 'darkorange', 'blue', 'green'][int(p)] for p in self.prediction_history]
            
    #         time_seconds = [t * 30 for t in self.time_history]
            
    #         self.ax.scatter(time_seconds, self.prediction_history, 
    #                        c=colors, s=50, alpha=0.7)
    #         self.ax.plot(time_seconds, self.prediction_history, 
    #                     'gray', alpha=0.3, linewidth=1)
        
    #     self.ax.set_yticks([0, 1, 2, 3])
    #     self.ax.set_yticklabels(['WAKE', 'LIGHT', 'DEEP', 'REM'])
    #     self.ax.set_ylabel('Sleep Stage')
    #     self.ax.set_xlabel('Time Since Start (seconds)')
    #     self.ax.set_title('Live Sleep Stage Predictions')
    #     self.ax.grid(True, alpha=0.3)
    #     self.ax.set_ylim(-0.5, 3.5)
        
    #     self.canvas.draw()
    
    def update_plot(self):
        """Update the live prediction plot with actual vs predicted"""
        self.ax.clear()
        
        # Plot actual sleep stages (transparent, for reference)
        if hasattr(self, 'actual_sleep_stages') and self.actual_sleep_stages:
            # Only show actual data up to current point to avoid data leakage
            current_max_time = self.epoch_counter * 30
            actual_times_to_show = [t for t in self.actual_times if t <= current_max_time]
            actual_stages_to_show = self.actual_sleep_stages[:len(actual_times_to_show)]
            
            if actual_times_to_show:
                self.ax.plot(actual_times_to_show, actual_stages_to_show, 
                            label='Actual Stage', color='blue', alpha=0.3, linewidth=2, drawstyle='steps-post')
        
        # Plot predictions (as before)
        if self.prediction_history:
            # Convert epoch numbers to time in seconds (30 seconds per epoch)
            time_seconds = [t * 30 for t in self.time_history]
            
            # Create colormap for sleep stages
            colors = [['red', 'darkorange', 'blue', 'green'][int(p)] for p in self.prediction_history]
            
            self.ax.scatter(time_seconds, self.prediction_history, 
                        c=colors, s=50, alpha=0.7, label='Predicted')
            self.ax.plot(time_seconds, self.prediction_history, 
                        'gray', alpha=0.3, linewidth=1)
        
        self.ax.set_yticks([0, 1, 2, 3])
        self.ax.set_yticklabels(['WAKE', 'LIGHT', 'DEEP', 'REM'])
        self.ax.set_ylabel('Sleep Stage')
        self.ax.set_xlabel('Time Since Start (seconds)')
        self.ax.set_title('Live Sleep Stage Predictions (Actual vs Predicted)')
        self.ax.grid(True, alpha=0.3)
        self.ax.set_ylim(-0.5, 3.5)
        
        # Only show legend if we have both actual and predicted data
        if self.prediction_history and hasattr(self, 'actual_sleep_stages') and self.actual_sleep_stages:
            self.ax.legend()
        
        self.canvas.draw()
    
    def start_auto_play(self, speed=1.0):
        """Start automatic playback"""
        if self.model is None or self.raw_data is None:
            messagebox.showwarning("Warning", "Please load both model and data first")
            return
        
        self.auto_play = True
        self.auto_play_speed = speed
        
        def auto_play_thread():
            while (self.auto_play and 
                   self.current_index < len(self.raw_data)):
                self.root.after(0, self.process_next_sample)
                time.sleep(1.0 * self.auto_play_speed)  # Simulate real-time with speed factor
        
        self.auto_play_thread = threading.Thread(target=auto_play_thread, daemon=True)
        self.auto_play_thread.start()
        
        self.log_status(f"▶ Started auto-play ({1/self.auto_play_speed:.1f}x speed)")
    
    def stop_auto_play(self):
        """Stop automatic playback"""
        self.auto_play = False
        self.log_status("⏹ Stopped auto-play")
    
    def reset_simulation(self):
        """Reset the simulation to start"""
        self.current_index = 0
        self.data_buffer = []
        self.epoch_counter = 0
        self.prediction_history.clear()
        self.time_history.clear()
        
        if hasattr(self, 'previous_epochs_buffer'):
            self.previous_epochs_buffer.clear()
        
        self.prediction_label.config(text="No prediction yet", foreground="black")
        self.confidence_label.config(text="", foreground="black")
        self.feature_text.delete(1.0, tk.END)
        self.update_plot()
        self.log_status("✓ Simulation reset")
        self.root.title("Live Sleep Stage Predictor")
    
    def log_status(self, message):
        """Add message to status log"""
        self.status_text.insert(tk.END, f"{message}\n")
        self.status_text.see(tk.END)
        self.root.update()

def main():
    root = tk.Tk()
    app = LiveSleepPredictor(root)
    root.mainloop()

if __name__ == "__main__":
    main()