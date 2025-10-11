package com.samsung.health.hrdatatransfer.data;

import com.google.android.gms.wearable.CapabilityClient;
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
public final class CapabilityRepositoryImpl_Factory implements Factory<CapabilityRepositoryImpl> {
  private final Provider<CapabilityClient> capabilityClientProvider;

  public CapabilityRepositoryImpl_Factory(Provider<CapabilityClient> capabilityClientProvider) {
    this.capabilityClientProvider = capabilityClientProvider;
  }

  @Override
  public CapabilityRepositoryImpl get() {
    return newInstance(capabilityClientProvider.get());
  }

  public static CapabilityRepositoryImpl_Factory create(
      Provider<CapabilityClient> capabilityClientProvider) {
    return new CapabilityRepositoryImpl_Factory(capabilityClientProvider);
  }

  public static CapabilityRepositoryImpl newInstance(CapabilityClient capabilityClient) {
    return new CapabilityRepositoryImpl(capabilityClient);
  }
}
