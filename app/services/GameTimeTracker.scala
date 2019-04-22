package services

import models.Game
import org.joda.time.{DateTime, DateTimeZone, Period}

object GameTimeTracker {

  def timeTrackGameInteraction(game: Game): Game = {
    val now = DateTime.now(DateTimeZone.UTC)
    val previousInteractionAt = game.latestInteractionAt
    val diffBetweenInteractions = new Period(previousInteractionAt, now).toStandardSeconds.getSeconds
    // time incremented game instance that takes latest interaction into account
    game.copy(latestInteractionAt = now, totalTime = game.totalTime + diffBetweenInteractions)
  }
}
