package com.socialapp.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static String getNewFilename(String origFilename)
	{
		int pos = origFilename.lastIndexOf(".");
		String ext = origFilename.substring(pos);
		UUID name = UUID.randomUUID();
		String filename = name + ext;
		
		return filename;
	}
	
	public static void saveFile(MultipartFile file, String userFileDir, String filename)
	{
		try(var in = file.getInputStream();
			var out = new FileOutputStream(userFileDir + filename))
		{
			in.transferTo(out);
		}		
		catch(IOException ex)
		{			
			throw new RuntimeException(ex);
		}
	}
	
	public static void deleteFile(String userFileDir, String filename)
	{
		try
		{
			Path path = Path.of(userFileDir, filename);
			System.out.println("path:" + path);
			Files.deleteIfExists(path);
		}
		catch(IOException ex)
		{
			logger.debug(ex.getMessage());
		}		
	}

}
