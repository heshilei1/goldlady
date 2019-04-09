package com.gold.util;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by hsl on 2017/11/3.
 */
public class MediaIdMessage {
    @XStreamAlias("MediaId")
    @XStreamCDATA
    private String MediaId;

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }

}
