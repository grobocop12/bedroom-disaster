package com.grobocop.bedroomdisaster.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(value = "/video")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
    private static final long chunkSize = 1024;

    @GetMapping("/full")
    public ResponseEntity<UrlResource> fullVideo() {
        try {
            UrlResource video = new UrlResource("file:video/hl_cat.mp4");
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaTypeFactory
                            .getMediaType(video)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .header("Content-Range", String.format("bytes %d-%d/%d", 0, video.contentLength() - 1, video.contentLength()))
                    .body(video);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @GetMapping("/partial")
    public ResponseEntity<ResourceRegion> partialVideo(@RequestHeader HttpHeaders headers) {
        HttpRange httpRange = headers.getRange()
                .stream()
                .findFirst()
                .orElse(null);
        try {
            UrlResource video = new UrlResource("file:video/pharaoh.mp4");
            long contentLength = video.contentLength();
            long start, end, rangeLength;
            ResourceRegion region;
            if (httpRange != null) {
                start = httpRange.getRangeStart(contentLength);
                end = httpRange.getRangeEnd(contentLength);
                rangeLength = Math.min(start + chunkSize, end - start + 1);
                region = new ResourceRegion(video, start, rangeLength);
            } else {
                start = 0;
                rangeLength = Math.min(chunkSize, contentLength);
                region = new ResourceRegion(video, 0, rangeLength);
            }
            //logger.info(String.format("%d - %d", start, rangeLength));
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaTypeFactory
                            .getMediaType(video)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .header("Content-Range", String.format("bytes %d-%d/%d", start, rangeLength, contentLength))
                    .body(region);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }


}
