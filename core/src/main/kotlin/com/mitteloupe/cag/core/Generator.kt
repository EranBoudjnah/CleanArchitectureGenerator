package com.mitteloupe.cag.core

import com.mitteloupe.cag.core.generation.bulk.ArchitectureFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.DataSourceFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.DataSourceModulesGenerator
import com.mitteloupe.cag.core.generation.bulk.FeatureFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.ProjectTemplateFilesGenerator
import com.mitteloupe.cag.core.generation.bulk.ViewModelFilesGenerator
import com.mitteloupe.cag.core.generation.layer.DomainLayerContentGenerator
import com.mitteloupe.cag.core.request.GenerateArchitectureRequest
import com.mitteloupe.cag.core.request.GenerateFeatureRequest
import com.mitteloupe.cag.core.request.GenerateProjectTemplateRequest
import com.mitteloupe.cag.core.request.GenerateUseCaseRequest
import com.mitteloupe.cag.core.request.GenerateViewModelRequest
import java.io.File

class Generator(
    private val domainLayerContentGenerator: DomainLayerContentGenerator,
    private val featureFilesGenerator: FeatureFilesGenerator,
    private val dataSourceModulesGenerator: DataSourceModulesGenerator,
    private val dataSourceFilesGenerator: DataSourceFilesGenerator,
    private val architectureFilesGenerator: ArchitectureFilesGenerator,
    private val projectTemplateFilesGenerator: ProjectTemplateFilesGenerator,
    private val viewModelFilesGenerator: ViewModelFilesGenerator
) {
    fun generateFeature(request: GenerateFeatureRequest) {
        featureFilesGenerator.generateFeature(
            featurePackageName = request.featurePackageName,
            featureName = request.featureName,
            projectNamespace = request.projectNamespace,
            destinationRootDirectory = request.destinationRootDirectory,
            appModuleDirectory = request.appModuleDirectory,
            dependencyInjection = request.dependencyInjection,
            enableCompose = request.enableCompose,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt
        )
    }

    fun generateUseCase(request: GenerateUseCaseRequest) {
        val destinationDirectory = request.destinationDirectory
        val useCaseName = request.useCaseName.trim()

        domainLayerContentGenerator
            .generateUseCase(
                destinationDirectory = destinationDirectory,
                useCaseName = useCaseName,
                inputDataType = request.inputDataType,
                outputDataType = request.outputDataType
            )
    }

    fun generateViewModel(request: GenerateViewModelRequest) {
        viewModelFilesGenerator.generateViewModel(
            request.destinationDirectory,
            request.viewModelName,
            request.viewModelPackageName,
            request.featurePackageName,
            request.projectNamespace
        )
    }

    fun generateDataSource(
        destinationRootDirectory: File,
        dataSourceName: String,
        projectNamespace: String,
        useKtor: Boolean = false,
        useRetrofit: Boolean = false
    ) {
        dataSourceModulesGenerator.generateDataSourceModules(
            destinationRootDirectory = destinationRootDirectory,
            useKtor = useKtor,
            useRetrofit = useRetrofit
        )

        dataSourceFilesGenerator.generateDataSource(
            destinationRootDirectory,
            dataSourceName,
            projectNamespace
        )
    }

    fun generateArchitecture(request: GenerateArchitectureRequest) {
        architectureFilesGenerator.generateArchitecture(
            projectNamespace = request.projectNamespace,
            destinationRootDirectory = request.destinationRootDirectory,
            appModuleDirectory = request.appModuleDirectory,
            architecturePackageName = request.architecturePackageName,
            dependencyInjection = request.dependencyInjection,
            enableCompose = request.enableCompose,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt
        )
    }

    fun generateProjectTemplate(request: GenerateProjectTemplateRequest) {
        projectTemplateFilesGenerator.generateProjectTemplate(
            destinationRootDirectory = request.destinationRootDirectory,
            projectName = request.projectName,
            packageName = request.packageName,
            overrideMinimumAndroidSdk = request.overrideMinimumAndroidSdk,
            overrideAndroidGradlePluginVersion = request.overrideAndroidGradlePluginVersion,
            dependencyInjection = request.dependencyInjection,
            enableCompose = request.enableCompose,
            enableKtlint = request.enableKtlint,
            enableDetekt = request.enableDetekt,
            enableKtor = request.enableKtor,
            enableRetrofit = request.enableRetrofit
        )
    }
}
