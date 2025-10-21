package com.samsung.health.mobile.presentation;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<MLModelInterface> mlModelInterfaceProvider;

  public MainActivity_MembersInjector(Provider<MLModelInterface> mlModelInterfaceProvider) {
    this.mlModelInterfaceProvider = mlModelInterfaceProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<MLModelInterface> mlModelInterfaceProvider) {
    return new MainActivity_MembersInjector(mlModelInterfaceProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectMlModelInterface(instance, mlModelInterfaceProvider.get());
  }

  @InjectedFieldSignature("com.samsung.health.mobile.presentation.MainActivity.mlModelInterface")
  public static void injectMlModelInterface(MainActivity instance,
      MLModelInterface mlModelInterface) {
    instance.mlModelInterface = mlModelInterface;
  }
}
