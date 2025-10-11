package com.samsung.health.mobile.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class RecordingRepository_Factory implements Factory<RecordingRepository> {
  private final Provider<DataRecorder> dataRecorderProvider;

  public RecordingRepository_Factory(Provider<DataRecorder> dataRecorderProvider) {
    this.dataRecorderProvider = dataRecorderProvider;
  }

  @Override
  public RecordingRepository get() {
    return newInstance(dataRecorderProvider.get());
  }

  public static RecordingRepository_Factory create(Provider<DataRecorder> dataRecorderProvider) {
    return new RecordingRepository_Factory(dataRecorderProvider);
  }

  public static RecordingRepository newInstance(DataRecorder dataRecorder) {
    return new RecordingRepository(dataRecorder);
  }
}
