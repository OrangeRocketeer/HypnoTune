package com.samsung.health.hrdatatransfer;

import com.google.android.gms.wearable.CapabilityClient;
import com.samsung.health.hrdatatransfer.data.CapabilityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class MainModule_ProvideCapabilitiesRepositoryFactory implements Factory<CapabilityRepository> {
  private final MainModule module;

  private final Provider<CapabilityClient> capabilityClientProvider;

  public MainModule_ProvideCapabilitiesRepositoryFactory(MainModule module,
      Provider<CapabilityClient> capabilityClientProvider) {
    this.module = module;
    this.capabilityClientProvider = capabilityClientProvider;
  }

  @Override
  public CapabilityRepository get() {
    return provideCapabilitiesRepository(module, capabilityClientProvider.get());
  }

  public static MainModule_ProvideCapabilitiesRepositoryFactory create(MainModule module,
      Provider<CapabilityClient> capabilityClientProvider) {
    return new MainModule_ProvideCapabilitiesRepositoryFactory(module, capabilityClientProvider);
  }

  public static CapabilityRepository provideCapabilitiesRepository(MainModule instance,
      CapabilityClient capabilityClient) {
    return Preconditions.checkNotNullFromProvides(instance.provideCapabilitiesRepository(capabilityClient));
  }
}
