package com.samsung.health.mobile.presentation;

import com.samsung.health.mobile.data.LiveDataRepository;
import com.samsung.health.mobile.data.MusicProfileManager;
import com.samsung.health.mobile.data.ProfileReminderManager;
import com.samsung.health.mobile.data.RecordingRepository;
import com.samsung.health.mobile.data.StageConfigManager;
import com.samsung.health.mobile.data.StageManager;
import com.samsung.health.mobile.ml.MLModelInterface;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<StageConfigManager> stageConfigManagerProvider;

  private final Provider<StageManager> stageManagerProvider;

  private final Provider<MLModelInterface> mlModelInterfaceProvider;

  private final Provider<MusicProfileManager> musicProfileManagerProvider;

  private final Provider<ProfileReminderManager> profileReminderManagerProvider;

  private final Provider<LiveDataRepository> liveDataRepositoryProvider;

  public MainViewModel_Factory(Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<StageConfigManager> stageConfigManagerProvider,
      Provider<StageManager> stageManagerProvider,
      Provider<MLModelInterface> mlModelInterfaceProvider,
      Provider<MusicProfileManager> musicProfileManagerProvider,
      Provider<ProfileReminderManager> profileReminderManagerProvider,
      Provider<LiveDataRepository> liveDataRepositoryProvider) {
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.stageConfigManagerProvider = stageConfigManagerProvider;
    this.stageManagerProvider = stageManagerProvider;
    this.mlModelInterfaceProvider = mlModelInterfaceProvider;
    this.musicProfileManagerProvider = musicProfileManagerProvider;
    this.profileReminderManagerProvider = profileReminderManagerProvider;
    this.liveDataRepositoryProvider = liveDataRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(recordingRepositoryProvider.get(), stageConfigManagerProvider.get(), stageManagerProvider.get(), mlModelInterfaceProvider.get(), musicProfileManagerProvider.get(), profileReminderManagerProvider.get(), liveDataRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<StageConfigManager> stageConfigManagerProvider,
      Provider<StageManager> stageManagerProvider,
      Provider<MLModelInterface> mlModelInterfaceProvider,
      Provider<MusicProfileManager> musicProfileManagerProvider,
      Provider<ProfileReminderManager> profileReminderManagerProvider,
      Provider<LiveDataRepository> liveDataRepositoryProvider) {
    return new MainViewModel_Factory(recordingRepositoryProvider, stageConfigManagerProvider, stageManagerProvider, mlModelInterfaceProvider, musicProfileManagerProvider, profileReminderManagerProvider, liveDataRepositoryProvider);
  }

  public static MainViewModel newInstance(RecordingRepository recordingRepository,
      StageConfigManager stageConfigManager, StageManager stageManager,
      MLModelInterface mlModelInterface, MusicProfileManager musicProfileManager,
      ProfileReminderManager profileReminderManager, LiveDataRepository liveDataRepository) {
    return new MainViewModel(recordingRepository, stageConfigManager, stageManager, mlModelInterface, musicProfileManager, profileReminderManager, liveDataRepository);
  }
}
