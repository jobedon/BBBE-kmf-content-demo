/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package com.kurento.demo.webrtc;

import com.kurento.kmf.content.WebRtcContentHandler;
import com.kurento.kmf.content.WebRtcContentService;
import com.kurento.kmf.content.WebRtcContentSession;
import com.kurento.kmf.media.MediaPipeline;
import com.kurento.kmf.media.WebRtcEndpoint;

/**
 * This handler implements a one to many video conference using WebRtcEnpoints;
 * the first session acts as "master", and the rest of concurrent sessions will
 * watch the "master" session in his remote stream; master's remote is a
 * loopback at the beginning, and it is changing with the stream of the each
 * participant in the conference.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 3.0.7
 * 
 * @Modified by Denny K. Schuldt
 */
@WebRtcContentService(path = "/streaming/*")
public class Streaming extends WebRtcContentHandler {

    private WebRtcEndpoint firstWebRtcEndpoint;
    private String sessionId;

    @Override
    public synchronized void onContentRequest(WebRtcContentSession contentSession) throws Exception {	
        if (firstWebRtcEndpoint == null) {
            MediaPipeline mp = contentSession.getMediaPipelineFactory().create();
            contentSession.releaseOnTerminate(mp);
            firstWebRtcEndpoint = mp.newWebRtcEndpoint().build();
            sessionId = contentSession.getSessionId();
            contentSession.releaseOnTerminate(firstWebRtcEndpoint);
            firstWebRtcEndpoint.connect(firstWebRtcEndpoint);
            contentSession.start(firstWebRtcEndpoint);
	} else {
            MediaPipeline mp = firstWebRtcEndpoint.getMediaPipeline();
            WebRtcEndpoint newWebRtcEndpoint = mp.newWebRtcEndpoint().build();
            contentSession.releaseOnTerminate(newWebRtcEndpoint);
            firstWebRtcEndpoint.connect(newWebRtcEndpoint);
            contentSession.start(newWebRtcEndpoint);
        }
    }

    @Override
    public void onContentStarted(WebRtcContentSession contentSession) {
        //Empty
    }

    @Override
    public void onSessionTerminated(WebRtcContentSession contentSession,int code, String reason) throws Exception {
        if (contentSession.getSessionId().equals(sessionId)) {
            getLogger().info("Terminating first WebRTC session");
            firstWebRtcEndpoint = null;
        }
        super.onSessionTerminated(contentSession, code, reason);
    }
}
