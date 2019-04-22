package models

import models.Board.Matrix
import play.api.libs.json.{JsString, JsValue, Json, Writes}
import services.BoardChecker

case class Position(x: Int, y: Int)

sealed trait CellType
case object Mine extends CellType
case object Hint extends CellType
case object Empty extends CellType

case class Cell(cellType: CellType, counter: Int = 0, isRevealed: Boolean = false, isFlaggedByUser: Boolean = false, isMarkedByUser: Boolean = false)

object Cell {

  val MineSymbol = "*"
  val FlaggedAsMineSymbol = "!"
  val QuestionMarkSymbol = "?"
  val UndisclosedSymbol = "-"

  def display(cell: Cell): String = {
    def displayUnrevealedCell(cell: Cell): String = {
      if (cell.isFlaggedByUser) FlaggedAsMineSymbol else if (cell.isMarkedByUser) QuestionMarkSymbol else UndisclosedSymbol
    }
    cell match {
      case Cell(Mine, _, _, _, _) if cell.isRevealed => MineSymbol
      case _ => if (cell.isRevealed) cell.counter.toString else displayUnrevealedCell(cell)
    }
  }
}

case class Board(cells: Matrix) {

  def rowCount: Int = cells.length

  def columnCount: Int = cells(0).length

  def cell(x: Int, y: Int): Cell = cells(x)(y)

  def update(x: Int, y: Int, newCellValue: Cell): Board = {
    val cellsCopy = copyCells
    cellsCopy(x)(y) = newCellValue
    Board(cellsCopy)
  }

  def reveal(x: Int, y: Int): Either[String, Board] = if (withinBounds(x, y)) Right(doReveal(x, y)) else Left("game.move.reveal.error")

  private def doReveal(x: Int, y: Int): Board = {
    val cell: Cell = this.cell(x, y)
    cell match {
      case Cell(_, _, true, _, _) => this // already revealed
      case Cell(Mine, _, _, _, _) | Cell(Hint, _, _, _, _) => doRevealCell(x, y, cell)
      case Cell(Empty,_, _, _, _) => doRevealCell(x, y, cell).revealNeighbours(x, y) // no adjacent mines, so we reveal neighbours
    }
  }

  private def doRevealCell(x: Int, y: Int, cell: Cell): Board = update(x, y, cell.copy(isRevealed = true, isFlaggedByUser = false, isMarkedByUser = false))

  private def revealNeighbours(x: Int, y: Int): Board = {
    val neighbourPositions = neighbourCellDetails(x, y).map(x => x._1)
    neighbourPositions.foldLeft(this) ((currentBoard, neighbourPosition) => currentBoard doReveal(neighbourPosition.x, neighbourPosition.y))
  }

  def flagAsMine(x: Int, y: Int): Either[String, Board] = {
    if (withinBounds(x, y) && !cell(x, y).isRevealed) {
      Right(update(x, y, cell(x, y).copy(isFlaggedByUser = true, isMarkedByUser = false)))
    } else { Left("game.move.flag.error") }
  }

  def questionMark(x: Int, y: Int): Either[String, Board] = {
    if (withinBounds(x, y) && !cell(x, y).isRevealed) {
      Right(update(x, y, cell(x, y).copy(isMarkedByUser = true, isFlaggedByUser = false)))
    } else { Left("game.move.mark.error") }
  }

  def neighbourCellDetails(x: Int, y: Int): Seq[(Position, Cell)] = {
    val neighbourPositions = BoardChecker.validNeighbourPositions(Position(x, y), rowCount, columnCount)
    neighbourPositions.map(pos => (pos, cell(pos.x, pos.y)))
  }

  def initAllCellsToDefaultValue(cellValue: Cell): Board = {
    val cells: Matrix = Array.tabulate(rowCount, columnCount)((_, _) => cellValue)
    Board(cells)
  }

  private def copyCells: Matrix = {
    val cellsCopy: Matrix = Array.ofDim[Cell](rowCount, columnCount)
    cells.copyToArray(cellsCopy)
    cellsCopy
  }

  private def withinBounds(x: Int, y: Int): Boolean = BoardChecker.withinBounds(x, y, rowCount, columnCount)

  def anyMinesRevealed: Boolean = cells.flatten.exists(cell => Mine == cell.cellType && cell.isRevealed)

  def allMinesAreFlagged: Boolean = !cells.flatten.exists(cell => Mine == cell.cellType && !cell.isFlaggedByUser)

  def allCellsThatAreNotMinesAreRevealed: Boolean = !cells.flatten.exists(cell => Mine != cell.cellType && !cell.isRevealed)

  /**
    * Full representation, includes detailed cell information
    */
  override def toString: String = {
    val array = Array.tabulate(rowCount, columnCount)((x, y) => {
      val cell = this.cell(x, y)
      val cellRecord = new StringBuilder
      if (!cell.isRevealed) cellRecord.append(Cell.UndisclosedSymbol)
      if (cell.isMarkedByUser) cellRecord.append(Cell.QuestionMarkSymbol)
      if (cell.isFlaggedByUser) cellRecord.append(Cell.FlaggedAsMineSymbol)
      cellRecord.append("(")
      cellRecord.append(cell.counter)
      cellRecord.append(")")
      cellRecord.toString
    })
    "[" + array.map(_.mkString(",")).mkString(",\r\n") + "]"
  }
}

object Board {

  type Matrix = Array[Array[Cell]]

  val MineCounterValue: Int = 100
  val CellCounterValueMatchExpr = "[\\(\\)]"

  implicit val writes = new Writes[Board] {
    def writes(board: Board): JsValue = {
      Json.arr(0 until board.rowCount map {
        row => {
          0 until board.columnCount map {
            col => JsString(Cell.display(board cell(row, col)))
          }
        }
      }).value(0)
    }
  }

  def apply(rowCount: Int, columnCount: Int): Board = new Board(Array.ofDim[Cell](rowCount, columnCount))

  def apply(rowCount: Int, columnCount: Int, contents: String): Board = {
    val emptyBoard = Board(rowCount, columnCount)
    val cellContents = contents.substring(1, contents.length - 1).split(",")
    val cells = cellContents.map(cellContent => {
      val counter = cellContent.split(CellCounterValueMatchExpr)(1).toInt
      val cellType = counter match {
        case MineCounterValue => Mine
        case 0 => Empty
        case _ => Hint
      }
      Cell(cellType, counter, isRevealed = !cellContent.contains(Cell.UndisclosedSymbol), isFlaggedByUser = cellContent.contains(Cell.FlaggedAsMineSymbol), isMarkedByUser = cellContent.contains(Cell.QuestionMarkSymbol))
    })
    // now obtained cell values will be used to fill the board
    val boardCellPositions = for (x <- 0 until rowCount; y <- 0 until columnCount) yield Position(x, y)
    val boardCellsByPosition = boardCellPositions zip cells

    // add all parsed cells to board
    boardCellsByPosition.foldLeft(emptyBoard)((board: Board, addCellDetail: (Position, Cell)) =>
      board update (x = addCellDetail._1.x, y = addCellDetail._1.y, newCellValue = addCellDetail._2))
  }
}