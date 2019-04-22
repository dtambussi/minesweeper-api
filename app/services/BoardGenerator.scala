package services

import models.Board.Matrix
import models._

import scala.util.Random

object BoardChecker {

  def validNeighbourPositions(position: Position, rowCount: Int, columnCount: Int): Seq[Position] = {
    val (x, y) =  (position.x, position.y)
    val candidates = Seq(
      Position(x -1, y + 1), Position(x, y + 1), Position(x + 1, y + 1), // above
      Position(x -1, y), Position(x + 1, y), // left right
      Position(x - 1, y - 1), Position(x, y - 1), Position(x + 1, y - 1) // down
    )
    candidates.filter(calculatedPos => withinBounds(calculatedPos.x, calculatedPos.y, rowCount, columnCount))
  }

  def withinBounds(x: Int, y: Int, rowCount: Int, columnCount: Int): Boolean = {
    val (maxRowIndex, maxColIndex) = (rowCount - 1, columnCount - 1)
    x <= maxRowIndex && x >= 0 && y <= maxColIndex && y >= 0
  }
}

object BoardGenerator {

  def generateBoard(rowCount: Int, columnCount: Int, mineCount: Int): Board = {
    val cells: Matrix = Array.tabulate(rowCount, columnCount)((_, _) => Cell(Empty))
    val allPositions = for(rowIndex <- 0 until rowCount; colIndex <- 0 until columnCount) yield Position(rowIndex, colIndex)
    val minePositions = Random.shuffle(allPositions).take(mineCount)
    // set mines and hints
    allPositions.foreach(position =>
      if (minePositions contains position) {
        cells(position.x)(position.y) = Cell(Mine, Board.MineCounterValue)
        // mine is now in matrix, affect neighbours now
        val neighbourPositions = BoardChecker.validNeighbourPositions(position, rowCount, columnCount)
        neighbourPositions.foreach(neighbourPosition => {
          val neighbourCell = cells(neighbourPosition.x)(neighbourPosition.y)
          neighbourCell match {
            case Cell(Mine, _, _, _, _) => ()
            case _ => cells(neighbourPosition.x)(neighbourPosition.y) = neighbourCell.copy(cellType = Hint, counter = neighbourCell.counter + 1)
          }
        })
      }
    )
    Board(cells)
  }
}