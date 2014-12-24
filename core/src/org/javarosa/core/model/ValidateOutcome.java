package org.javarosa.core.model;

public class ValidateOutcome {
   public final FormIndex failedPrompt;
   public final int outcome;
   
   ValidateOutcome(FormIndex failedPrompt, int outcome) {
      this.failedPrompt = failedPrompt;
      this.outcome = outcome;
   }
}