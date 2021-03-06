/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;
import org.eclipse.che.plugin.maven.shared.dto.PercentMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.PercentUndefinedMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;
import org.eclipse.che.plugin.maven.shared.dto.TextMessageDto;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_OUTPUT;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ARCHETYPE_CHANEL_SUBSCRIBE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_PERCENT_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_PERCENT_UNDEFINED_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_START_STOP_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_TEXT_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_UPDATE_METHOD;

/**
 * A mechanism for handling all messages from the Maven server and applying registered consumers.
 */
@Singleton
public class MavenJsonRpcHandler {
    private static final String WS_AGENT_ENDPOINT = "ws-agent";

    private RequestHandlerConfigurator configurator;

    private Set<Consumer<TextMessageDto>>             textConsumers             = new HashSet<>();
    private Set<Consumer<StartStopNotification>>      startStopConsumers        = new HashSet<>();
    private Set<Consumer<PercentUndefinedMessageDto>> percentUndefinedConsumers = new HashSet<>();
    private Set<Consumer<PercentMessageDto>>          percentConsumers          = new HashSet<>();
    private Set<Consumer<ProjectsUpdateMessage>>      projectsUpdateConsumers   = new HashSet<>();
    private Set<Consumer<ArchetypeOutput>>            archetypeOutputConsumers  = new HashSet<>();

    private boolean isSubscribed = false;

    @Inject
    public MavenJsonRpcHandler(RequestHandlerConfigurator configurator) {
        this.configurator = configurator;

        handleMavenServerMessages();
    }

    @Inject
    private void subscribe(RequestTransmitter requestTransmitter) {
        if (isSubscribed) {
            return;
        }

        requestTransmitter.newRequest()
                          .endpointId(WS_AGENT_ENDPOINT)
                          .methodName(MAVEN_OUTPUT_SUBSCRIBE)
                          .noParams()
                          .sendAndSkipResult();

        requestTransmitter.newRequest()
                          .endpointId(WS_AGENT_ENDPOINT)
                          .methodName(MAVEN_ARCHETYPE_CHANEL_SUBSCRIBE)
                          .noParams()
                          .sendAndSkipResult();

        isSubscribed = true;
    }

    /**
     * Adds consumer for the event with {@link TextMessageDto}.
     *
     * @param consumer
     *         new consumer
     */
    public void addTextHandler(Consumer<TextMessageDto> consumer) {
        textConsumers.add(consumer);
    }

    /**
     * Adds consumer for the event with {@link StartStopNotification}.
     *
     * @param consumer
     *         new consumer
     */
    public void addStartStopHandler(Consumer<StartStopNotification> consumer) {
        startStopConsumers.add(consumer);
    }

    /**
     * Adds consumer for the event with {@link PercentUndefinedMessageDto}.
     *
     * @param consumer
     *         new consumer
     */
    public void addPercentUndefinedHandler(Consumer<PercentUndefinedMessageDto> consumer) {
        percentUndefinedConsumers.add(consumer);
    }

    /**
     * Adds consumer for the event with {@link PercentMessageDto}.
     *
     * @param consumer
     *         new consumer
     */
    public void addPercentHandler(Consumer<PercentMessageDto> consumer) {
        percentConsumers.add(consumer);
    }

    /**
     * Adds consumer for the event with {@link ProjectsUpdateMessage}.
     *
     * @param consumer
     *         new consumer
     */
    public void addProjectsUpdateHandler(Consumer<ProjectsUpdateMessage> consumer) {
        projectsUpdateConsumers.add(consumer);
    }

    /**
     * Adds consumer for the event with {@link ArchetypeOutput}.
     *
     * @param consumer
     *         new consumer
     */
    public void addArchetypeOutputHandler(Consumer<ArchetypeOutput> consumer) {
        archetypeOutputConsumers.add(consumer);
    }

    private void handleMavenServerMessages() {
        configurator.newConfiguration()
                    .methodName(MAVEN_OUTPUT_TEXT_METHOD)
                    .paramsAsDto(TextMessageDto.class)
                    .noResult()
                    .withConsumer(textNotification -> textConsumers.forEach(it -> it.accept(textNotification)));

        configurator.newConfiguration()
                    .methodName(MAVEN_OUTPUT_PERCENT_UNDEFINED_METHOD)
                    .paramsAsDto(PercentUndefinedMessageDto.class)
                    .noResult()
                    .withConsumer(percentUndefinedMessage -> percentUndefinedConsumers.forEach(it -> it.accept(percentUndefinedMessage)));

        configurator.newConfiguration()
                    .methodName(MAVEN_OUTPUT_PERCENT_METHOD)
                    .paramsAsDto(PercentMessageDto.class)
                    .noResult()
                    .withConsumer(percentMessage -> percentConsumers.forEach(it -> it.accept(percentMessage)));

        configurator.newConfiguration()
                    .methodName(MAVEN_OUTPUT_START_STOP_METHOD)
                    .paramsAsDto(StartStopNotification.class)
                    .noResult()
                    .withConsumer(startStopNotification -> startStopConsumers.forEach(it -> it.accept(startStopNotification)));

        configurator.newConfiguration()
                    .methodName(MAVEN_OUTPUT_UPDATE_METHOD)
                    .paramsAsDto(ProjectsUpdateMessage.class)
                    .noResult()
                    .withConsumer(projectsUpdateMessage -> projectsUpdateConsumers.forEach(it -> it.accept(projectsUpdateMessage)));

        configurator.newConfiguration()
                    .methodName(MAVEN_ARCHETYPE_CHANEL_OUTPUT)
                    .paramsAsDto(ArchetypeOutput.class)
                    .noResult()
                    .withConsumer(archetypeOutput -> archetypeOutputConsumers.forEach(it -> it.accept(archetypeOutput)));
    }
}
