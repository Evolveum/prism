/*
 * Copyright (c) 2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism.codegen.maven;

import java.io.*;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.impl.PrismContextImpl;
import com.evolveum.midpoint.prism.impl.schema.SchemaDefinitionFactory;
import com.evolveum.midpoint.prism.impl.schema.SchemaRegistryImpl;
import com.evolveum.midpoint.prism.impl.xml.GlobalDynamicNamespacePrefixMapper;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.prism.codegen.binding.BindingContext;
import com.evolveum.prism.codegen.binding.NamespaceConstantMapping;
import com.evolveum.prism.codegen.impl.CodeGenerator;

@Mojo(name="codegen", requiresDependencyResolution = ResolutionScope.COMPILE)
@Execute(goal="cogegen", phase = LifecyclePhase.GENERATE_SOURCES)
public class PrismCodegenMojo extends AbstractMojo {

    @Parameter
    private File[] schemaFiles;

    @Parameter
    private File[] wsdlFiles;

    @Parameter
    private File[] catalogFiles;

    @Parameter
    private Constant[] constants;

    @Parameter(defaultValue="${project.build.directory}", required=true)
    private File buildDir;

    @Parameter(defaultValue="${project.build.directory}/generated-sources/prism", required=true)
    private File destDir;

    @Parameter(defaultValue="src/main/schemadoc/templates", required=true)
    private File templateDir;

    @Parameter(defaultValue="src/main/schemadoc/resources")
    private File resourcesDir;

    @Parameter(defaultValue="${project}")
    private org.apache.maven.project.MavenProject project;

    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    @Parameter(defaultValue="${project.build.finalName}")
    private String finalName;

    @Component
    private MavenProjectHelper projectHelper;

    @Component(role=Archiver.class, hint="zip")
    private ZipArchiver zipArchiver;

    private String getTemplateDirName() {
        return templateDir.getAbsolutePath();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug( "CodeGen plugin started" );

        PrismContext prismContext = createInitializedPrismContext();

        File outDir = initializeOutDir();
        SchemaRegistry schemaRegistry = prismContext.getSchemaRegistry();


        BindingContext context = new BindingContext();

        context.addSchemas(schemaRegistry.getSchemas());

        if (constants != null) {
            for (NamespaceConstantMapping constant : constants) {
                context.addConstantMapping(constant);
            }
        }

        context.process();

        try {
            CodeGenerator generator = new CodeGenerator(outDir, context);
            generator.process();
            generator.write();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
        project.addCompileSourceRoot(destDir.getPath());

        getLog().debug( "CodeGen plugin finished" );
    }

    private File initializeOutDir() throws MojoFailureException {
        getLog().debug("Output dir: "+destDir);
        if ( destDir.exists() && !destDir.isDirectory() ) {
            throw new MojoFailureException("Destination directory is not a directory: "+destDir);
        }
        if (destDir.exists() && !destDir.canWrite()) {
            throw new MojoFailureException("Destination directory is not writable: "+destDir);
        }
        destDir.mkdirs();
        return destDir;
    }

    private PrismContext createInitializedPrismContext() throws MojoFailureException {
        try {
            SchemaRegistryImpl schemaRegistry = createSchemaRegistry();

            schemaRegistry.registerPrismSchemaResource("xml/ns/public/types-3.xsd", "t", null);
            schemaRegistry.registerPrismSchemaResource("xml/ns/public/query-3.xsd", "q", null);


            for (File schemaFile: schemaFiles) {
                getLog().debug("SchemaDoc: registering schema file: "+schemaFile);
                if (!schemaFile.exists()) {
                    throw new MojoFailureException("Schema file "+schemaFile+" does not exist");
                }
                schemaRegistry.registerPrismSchemaFile(schemaFile);
            }

            if (wsdlFiles != null) {
                for (File wsdlFile : wsdlFiles) {
                    getLog().debug("SchemaDoc: registering WSDL file: "+ wsdlFile);
                    if (!wsdlFile.exists()) {
                        throw new MojoFailureException("WSDLfile "+wsdlFile+" does not exist");
                    }
                    schemaRegistry.registerWsdlSchemaFile(wsdlFile);
                }
            }

            if (catalogFiles != null && catalogFiles.length > 0) {
                for (File catalogFile : catalogFiles) {
                    getLog().debug("SchemaDoc: using catalog file: " + catalogFile);
                    if (!catalogFile.exists()) {
                        throw new IOException("Catalog file '" + catalogFile + "' does not exist.");
                    }
                }
                schemaRegistry.setCatalogFiles(catalogFiles);
            }

            PrismContextImpl context = PrismContextImpl.create(schemaRegistry);
            context.setDefinitionFactory(new SchemaDefinitionFactory());
            context.initialize();

            return context;

        } catch (SchemaException e) {
            handleFailure(e);
            // never reached
            return null;
        } catch (FileNotFoundException e) {
            handleFailure(e);
            // never reached
            return null;
        } catch (SAXException e) {
            handleFailure(e);
            // never reached
            return null;
        } catch (IOException e) {
            handleFailure(e);
            // never reached
            return null;
        }
    }

    private void handleFailure(Exception e) throws MojoFailureException {
        e.printStackTrace();
        throw new MojoFailureException(e.getMessage());
    }

    @NotNull
    private SchemaRegistryImpl createSchemaRegistry() throws SchemaException {
        SchemaRegistryImpl schemaRegistry = new SchemaRegistryImpl();
        schemaRegistry.setNamespacePrefixMapper(new GlobalDynamicNamespacePrefixMapper());
        return schemaRegistry;
    }

    private void copyResources(File outDir) throws IOException {
        if (resourcesDir.exists()) {
            MiscUtil.copyDirectory(resourcesDir, outDir);
        }
    }

}
