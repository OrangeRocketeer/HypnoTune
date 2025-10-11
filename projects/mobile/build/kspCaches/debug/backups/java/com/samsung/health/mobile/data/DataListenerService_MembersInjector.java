package com.samsung.health.mobile.data;

import com.samsung.health.mobile.ml.MLModelInterface;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class DataListenerService_MembersInjector implements MembersInjector<DataListenerService> {
  private final Provider<RecordingRepository> recordingRepositoryProvider;

  private final Provider<MLModelInterface> mlModelInterfaceProvider;

  private final Provider<StageManager> stageManagerProvider;

  public DataListenerService_MembersInjector(
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<MLModelInterface> mlModelInterfaceProvider,
      Provider<StageManager> stageManagerProvider) {
    this.recordingRepositoryProvider = recordingRepositoryProvider;
    this.mlModelInterfaceProvider = mlModelInterfaceProvider;
    this.stageManagerProvider = stageManagerProvider;
  }

  public static MembersInjector<DataListenerService> create(
      Provider<RecordingRepository> recordingRepositoryProvider,
      Provider<MLModelInterface> mlModelInterfaceProvider,
      Provider<StageManager> stageManagerProvider) {
    return new DataListenerService_MembersInjector(recordingRepositoryProvider, mlModelInterfaceProvider, stageManagerProvider);
  }

  @Override
  public void injectMembers(DataListenerService instance) {
    injectRecordingRepository(instance, recordingRepositoryProvider.get());
    injectMlModelInterface(instance, mlModelInterfaceProvider.get());
    injectStageManager(instance, stageManagerProvider.get());
  }

  @InjectedFieldSignature("com.samsung.health.mobile.data.DataListenerService.recordingRepository")
  public static void injectRecordingRepository(DataListenerService instance,
      RecordingRepository recordingRepository) {
    instance.recordingRepository = recordingRepository;
  }

  @InjectedFieldSignature("com.samsung.health.mobile.data.DataListenerService.mlModelInterface")
  public static void injectMlModelInterface(DataListenerService instance,
      MLModelInterface mlModelInterface) {
    instance.mlModelInterface = mlModelInterface;
  }

  @InjectedFieldSignature("com.samsung.health.mobile.data.DataListenerService.stageManager")
  public static void injectStageManager(DataListenerService instance, StageManager stageManager) {
    instance.stageManager = stageManager;
  }
}
