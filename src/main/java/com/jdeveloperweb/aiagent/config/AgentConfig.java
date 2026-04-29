package com.jdeveloperweb.aiagent.config;

import com.jdeveloperweb.aiagent.agent.CodeAgent;
import com.jdeveloperweb.aiagent.agent.CriticAgent;
import com.jdeveloperweb.aiagent.agent.OrchestratorAgent;
import com.jdeveloperweb.aiagent.agent.SectionAgent;
import com.jdeveloperweb.aiagent.agent.UncertaintyAgent;
import com.jdeveloperweb.aiagent.tool.CodeGeneratorTool;
import com.jdeveloperweb.aiagent.tool.ConfidenceTool;
import com.jdeveloperweb.aiagent.tool.ConfluenceSearchTool;
import com.jdeveloperweb.aiagent.tool.CriticTool;
import com.jdeveloperweb.aiagent.tool.EpisodicMemoryTool;
import com.jdeveloperweb.aiagent.tool.FileSystemTool;
import com.jdeveloperweb.aiagent.tool.RagTool;
import com.jdeveloperweb.aiagent.tool.SectionWriterTool;
import com.jdeveloperweb.aiagent.tool.TaskManagerTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AgentConfig {


    @Bean
    public SectionAgent sectionAgent(ChatModel chatModel,
                                     RagTool ragTool,
                                     EpisodicMemoryTool episodicMemoryTool,
                                     ConfluenceSearchTool confluenceSearchTool) {

        return AiServices.builder(SectionAgent.class)
                .chatModel(chatModel)
                .tools(ragTool, episodicMemoryTool, confluenceSearchTool)
                .build();
    }

    @Bean
    public CriticAgent criticAgent(ChatModel chatModel) {
        return AiServices.builder(CriticAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public UncertaintyAgent uncertaintyAgent(ChatModel chatModel) {
        return AiServices.builder(UncertaintyAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public CodeAgent codeAgent(ChatModel chatModel,
                               FileSystemTool fileSystemTool) {
        return AiServices.builder(CodeAgent.class)
                .chatModel(chatModel)
                .tools(fileSystemTool)
                .build();
    }

    @Bean
    public OrchestratorAgent orchestratorAgent(ChatModel chatModel,
                                               TaskManagerTool taskManagerTool,
                                               SectionWriterTool sectionWriterTool,
                                               CriticTool criticTool,
                                               ConfidenceTool confidenceTool,
                                               CodeGeneratorTool codeGeneratorTool,
                                               RagTool ragTool,
                                               EpisodicMemoryTool episodicMemoryTool,
                                               FileSystemTool fileSystemTool,
                                               ConfluenceSearchTool confluenceSearchTool) {

        return AiServices.builder(OrchestratorAgent.class)
                .chatModel(chatModel)
                .tools(taskManagerTool, sectionWriterTool, criticTool,
                       confidenceTool, codeGeneratorTool, ragTool, episodicMemoryTool, fileSystemTool, confluenceSearchTool)
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.withMaxMessages(60))
                .build();
    }
}
