package com.samsung.health.hrdatatransfer;

import android.content.Context;
import com.samsung.health.hrdatatransfer.data.AccelerometerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MainModule_ProvideAccelerometerRepositoryFactory implements Factory<AccelerometerRepository> {
  private final MainModule module;

  private final Provider<Context> contextProvider;

  public MainModule_ProvideAccelerometerRepositoryFactory(MainModule module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public AccelerometerRepository get() {
    return provideAccelerometerRepository(module, contextProvider.get());
  }

  public static MainModule_ProvideAccelerometerRepositoryFactory create(MainModule module,
      Provider<Context> contextProvider) {
    return new MainModule_ProvideAccelerometerRepositoryFactory(module, contextProvider);
  }

  public static AccelerometerRepository provideAccelerometerRepository(MainModule instance,
      Context context) {
    return Preconditions.checkNotNullFromProvides(instance.provideAccelerometerRepository(context));
  }
}
