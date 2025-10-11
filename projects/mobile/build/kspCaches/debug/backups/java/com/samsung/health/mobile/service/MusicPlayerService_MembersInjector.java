package com.samsung.health.mobile.service;

import com.samsung.health.mobile.data.StageConfigManager;
import com.samsung.health.mobile.data.StageManager;
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
public final class MusicPlayerService_MembersInjector implements MembersInjector<MusicPlayerService> {
  private final Provider<StageManager> stageManagerProvider;

  private final Provider<StageConfigManager> stageConfigManagerProvider;

  public MusicPlayerService_MembersInjector(Provider<StageManager> stageManagerProvider,
      Provider<StageConfigManager> stageConfigManagerProvider) {
    this.stageManagerProvider = stageManagerProvider;
    this.stageConfigManagerProvider = stageConfigManagerProvider;
  }

  public static MembersInjector<MusicPlayerService> create(
      Provider<StageManager> stageManagerProvider,
      Provider<StageConfigManager> stageConfigManagerProvider) {
    return new MusicPlayerService_MembersInjector(stageManagerProvider, stageConfigManagerProvider);
  }

  @Override
  public void injectMembers(MusicPlayerService instance) {
    injectStageManager(instance, stageManagerProvider.get());
    injectStageConfigManager(instance, stageConfigManagerProvider.get());
  }

  @InjectedFieldSignature("com.samsung.health.mobile.service.MusicPlayerService.stageManager")
  public static void injectStageManager(MusicPlayerService instance, StageManager stageManager) {
    instance.stageManager = stageManager;
  }

  @InjectedFieldSignature("com.samsung.health.mobile.service.MusicPlayerService.stageConfigManager")
  public static void injectStageConfigManager(MusicPlayerService instance,
      StageConfigManager stageConfigManager) {
    instance.stageConfigManager = stageConfigManager;
  }
}
