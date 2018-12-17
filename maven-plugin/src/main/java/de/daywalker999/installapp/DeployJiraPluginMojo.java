package de.daywalker999.installapp;

import com.bigbrassband.util.atlas.installapp.Main;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONObject;

import java.io.File;

@Mojo(name = "install")
public class DeployJiraPluginMojo extends AbstractMojo
{

	@Parameter(property = "install.username", defaultValue = "admin")
	private String username;

	@Parameter(property = "install.password", defaultValue = "admin")
	private String password;

	@Parameter(property = "install.baseUrl", required = true)
	private String baseUrl;

	@Parameter(property = "install.jarName")
	private String jarFile;

	@Parameter(defaultValue = "${project.build.directory}")
	private String projectBuildDir;

	@Parameter(defaultValue = "${project.artifactId}")
	private String artifactId;

	@Parameter(defaultValue = "${project.version}")
	private String version;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		File appFile = getJarFile();

		JSONObject config = new JSONObject();

		config.put("baseUrl", this.baseUrl);
		config.put("username", this.username);
		config.put("password", this.password);
		config.put("appFile", appFile.getAbsolutePath());

		try
		{

			getLog().info("Deploying to " + this.baseUrl + " as " + this.username);
			getLog().info("Target file is: " + appFile.getAbsolutePath());
			Main.performUpload(config);
			getLog().info("Done");
		} catch (Exception e)
		{
			getLog().error(e.getMessage(), e);
			throw new MojoFailureException(e.getMessage());
		}

	}


	private File getJarFile()
	{
		if (this.jarFile == null)
		{
			return new File(projectBuildDir, artifactId + "-" + version + ".jar");
		} else
		{
			return new File(projectBuildDir, jarFile + (jarFile.toLowerCase().endsWith(".jar") ? "" : ".jar"));
		}
	}
}
