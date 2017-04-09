package net.starfind.forumextractor.request.http;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import net.starfind.forumextractor.parser.ForumParser;
import net.starfind.forumextractor.parser.TopicParser;
import net.starfind.forumextractor.request.RequestService;

@Component
public class HttpApplicationLauncher implements ApplicationRunner {
	
	private static Logger LOG = LoggerFactory.getLogger(HttpApplicationLauncher.class);
	
	@Autowired
	private ForumParser forumParser;
	
	@Autowired
	private TopicParser topicParser;
	
	@Autowired
	private HttpConfiguration config;
	
	@Autowired
	private RequestService requester;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		List<String> forumIds = args.getOptionValues("forum");
		List<String> topicIds = args.getOptionValues("topic");
		
		if (forumIds != null && forumIds.isEmpty()) {
			LOG.info("Requesting forums: "+forumIds+" from "+config.getBaseUrl());
			
			for (String forumId : forumIds) {
				String forumPath = config.getForumPath().replace("<id>", forumId);
				
				URL url = new URL(config.getBaseUrl()+forumPath);
				
				requester.requestPage(url, is -> {
					try {
						forumParser.parseForumPage(is);
					} catch (IOException ex) {
						LOG.error("Problem parsing forum "+url, ex);
					}
				});
			}
		} else if (topicIds != null && topicIds.isEmpty()) {
			LOG.info("Requesting topics: "+topicIds+" from "+config.getBaseUrl());
			
			for (String topicId : topicIds) {
				String topicPath = config.getForumPath().replace("<id>", topicId);
				
				URL url = new URL(config.getBaseUrl()+topicPath);
				
				requester.requestPage(url, is -> {
					try {
						topicParser.parseTopicPage(is);
					} catch (IOException ex) {
						LOG.error("Problem parsing topic "+url, ex);
					}
				});
			}
		} else {
			System.err.println("Requires either --forum=<id> or --topic=<id>");
			System.exit(-1);
			return;
		}
	}
}