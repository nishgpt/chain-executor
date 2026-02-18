package com.github.nishgpt.chainexecutor.core.models.stage;

import com.github.nishgpt.chainexecutor.core.exceptions.ChainExecutorException;
import com.github.nishgpt.chainexecutor.core.exceptions.ErrorCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StageChainRegistry<T extends Stage, K extends StageChainIdentifier> extends
    HashMap<K, StageChain<T>> {

  public StageChain<T> getStageChain(K chainIdentifier) {
    return this.get(chainIdentifier);
  }

  public T getNextStage(K chainIdentifier,
      T currentStage) {
    final var chain = getStageChain(chainIdentifier);
    if (Objects.isNull(chain)) {
      return null;
    }
    // To init the first stage
    if (Objects.isNull(currentStage)) {
      return chain.getHead();
    }
    return chain.getForwardChainMappings()
        .get(currentStage);
  }

  public T getChainHead(K chainIdentifier) {
    final var chain = getStageChain(chainIdentifier);
    if (Objects.isNull(chain)) {
      return null;
    }
    return chain.getHead();
  }

  public void validateAllChains() {
    this.forEach(this::validateChain);
  }

  private void validateChain(
      final K identifier,
      final StageChain<T> chain
  ) {
    final Map<T, Boolean> allStages = new HashMap<>();

    chain.getForwardChainMappings()
        .forEach((key, value) -> {
          allStages.putIfAbsent(key, Boolean.FALSE);
          allStages.putIfAbsent(value, Boolean.FALSE);
        });

    var currentStage = chain.getHead();
    do {
      //If already visited the stage
      if (allStages.get(currentStage)) {
        log.error("Stage already encountered, possible loop; Invalid chain for {}", identifier);
        throw ChainExecutorException.error(ErrorCode.INVALID_CHAIN);
      }

      //set this stage as visited
      allStages.put(currentStage, Boolean.TRUE);

      //move over to next stage
      currentStage = chain.getForwardChainMappings()
          .get(currentStage);
    } while (Objects.nonNull(currentStage));

    //If any stage is left unvisited
    if (allStages.entrySet()
        .stream()
        .anyMatch(entry -> entry.getValue()
            .equals(Boolean.FALSE))) {
      log.error("Possible chain breakage; Invalid chain for {}", identifier);
      throw ChainExecutorException.error(ErrorCode.INVALID_CHAIN);
    }
  }
}
