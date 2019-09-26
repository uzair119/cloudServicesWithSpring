/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

@Controller
public class VideoController {
	
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	private Map<Long,Video> videos = new HashMap<Long, Video>();

  	public Video save(Video entity) {
		checkAndSetId(entity);
		entity.setDataUrl(getDataUrl(entity.getId()));
		videos.put(entity.getId(), entity);
		return entity;
	}

	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	
	private String getDataUrl(long videoId){
        String url = "http://localhost:8080/video/" + videoId + "/data";
        return url;
    }

 	
	
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */

	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList()
	{
		return videos.values();
	}
	
	@RequestMapping(value="/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(
			@RequestBody Video video
			)
	{
		return save(video);
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile videoData, HttpServletResponse response) throws IOException
	{
		
		Video v = videos.get(id);
		VideoStatus status = new VideoStatus(VideoState.READY);
		if(v == null)
		{
			response.setStatus(HttpStatus.NOT_FOUND_404);
			return status;
		}
		VideoFileManager vfm = VideoFileManager.get();
		vfm.saveVideoData(v, videoData.getInputStream());
		return status;
	}

	
	
    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
    public HttpServletResponse getData(@PathVariable("id") long id, HttpServletResponse response) throws IOException
    {
    	Video v = videos.get(id);
    	
		if(v == null)
		{
			response.sendError(HttpStatus.NOT_FOUND_404);
		}
    	response.setContentType("multipart/form-data");
    	VideoFileManager vfm = VideoFileManager.get();
    	OutputStream out = response.getOutputStream();
    	if(vfm.hasVideoData(v))	
    	{
    		vfm.copyVideoData(v, out);
    		response.setStatus(HttpStatus.OK_200);
    	}
    	return response;
    }
	
}
