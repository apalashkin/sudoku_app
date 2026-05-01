package com.apalashkin.sudoku.ui.game

import com.apalashkin.sudoku.domain.generator.Difficulty
import com.apalashkin.sudoku.domain.generator.Puzzle
import com.apalashkin.sudoku.domain.generator.PuzzleGenerator
import com.apalashkin.sudoku.domain.model.Board
import com.apalashkin.sudoku.domain.model.Cell
import com.apalashkin.sudoku.domain.model.Coord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameUiStateTest {

    private fun seededPuzzle(difficulty: Difficulty = Difficulty.EASY, seed: Long = 1L): Puzzle =
        PuzzleGenerator(Random(seed)).generate(difficulty)

    @Test
    fun `initial state has no selection and is not complete`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertNull(state.selected)
        assertFalse(state.isComplete)
    }

    @Test
    fun `selectCell updates the selection`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
            .selectCell(Coord(3, 4))
        assertEquals(Coord(3, 4), state.selected)
    }

    @Test
    fun `placeDigit with no selection is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertEquals(state, state.placeDigit(5))
    }

    @Test
    fun `placeDigit on a given cell is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val givenCoord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(givenCoord).placeDigit(5)
        assertEquals(state.puzzle.cell(givenCoord).value, after.puzzle.cell(givenCoord).value)
    }

    @Test
    fun `placeDigit on an empty cell writes the value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state.selectCell(emptyCoord).placeDigit(7)
        assertEquals(7, after.puzzle.cell(emptyCoord).value)
        assertFalse(after.puzzle.cell(emptyCoord).isGiven)
    }

    @Test
    fun `placeDigit replaces a user-filled value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state
            .selectCell(emptyCoord)
            .placeDigit(3)
            .placeDigit(7)
        assertEquals(7, after.puzzle.cell(emptyCoord).value)
    }

    @Test
    fun `placeDigit rejects value outside 1 to 9`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
            .selectCell(firstEmptyCoord(
                GameUiState.fromPuzzle(seededPuzzle()).puzzle
            ))
        assertThrows(IllegalArgumentException::class.java) { state.placeDigit(0) }
        assertThrows(IllegalArgumentException::class.java) { state.placeDigit(10) }
    }

    @Test
    fun `erase with no selection is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertEquals(state, state.erase())
    }

    @Test
    fun `erase on a given cell is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val givenCoord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(givenCoord).erase()
        assertEquals(state.puzzle.cell(givenCoord).value, after.puzzle.cell(givenCoord).value)
    }

    @Test
    fun `erase clears a user-filled value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val emptyCoord = firstEmptyCoord(state.puzzle)
        val after = state
            .selectCell(emptyCoord)
            .placeDigit(7)
            .erase()
        assertNull(after.puzzle.cell(emptyCoord).value)
    }

    @Test
    fun `placing the last correct digit marks the game complete`() {
        val puzzle = seededPuzzle()
        val almostSolved = buildAlmostSolvedState(puzzle, leaveEmpty = 1)
        val lastCoord = firstEmptyCoord(almostSolved.puzzle)
        val correctValue = puzzle.solution.cell(lastCoord).value!!
        val after = almostSolved.selectCell(lastCoord).placeDigit(correctValue)
        assertTrue(after.isComplete)
    }

    @Test
    fun `placing a wrong digit does not mark the game complete`() {
        val puzzle = seededPuzzle()
        val almostSolved = buildAlmostSolvedState(puzzle, leaveEmpty = 1)
        val lastCoord = firstEmptyCoord(almostSolved.puzzle)
        val correctValue = puzzle.solution.cell(lastCoord).value!!
        val wrongValue = if (correctValue == 9) 1 else correctValue + 1
        val after = almostSolved.selectCell(lastCoord).placeDigit(wrongValue)
        assertFalse(after.isComplete)
    }

    @Test
    fun `placeDigit is a no-op once the game is complete`() {
        val puzzle = seededPuzzle()
        val state = buildAlmostSolvedState(puzzle, leaveEmpty = 1).let {
            val coord = firstEmptyCoord(it.puzzle)
            it.selectCell(coord).placeDigit(puzzle.solution.cell(coord).value!!)
        }
        assertTrue(state.isComplete)
        val coord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(coord).placeDigit(1)
        assertEquals(state.puzzle.cell(coord).value, after.puzzle.cell(coord).value)
    }

    @Test
    fun `mistakes is empty on a clean board`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertTrue(state.mistakes().isEmpty())
    }

    @Test
    fun `mistakes flags both cells of a row conflict`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.filled(5))
            .withCell(Coord(0, 5), Cell.filled(5))
        val state = GameUiState(puzzle = board, solution = Board.empty())
        val m = state.mistakes()
        assertTrue(Coord(0, 0) in m)
        assertTrue(Coord(0, 5) in m)
    }

    @Test
    fun `mistakes flags column conflicts`() {
        val board = Board.empty()
            .withCell(Coord(0, 3), Cell.filled(7))
            .withCell(Coord(5, 3), Cell.filled(7))
        val state = GameUiState(puzzle = board, solution = Board.empty())
        val m = state.mistakes()
        assertTrue(Coord(0, 3) in m)
        assertTrue(Coord(5, 3) in m)
    }

    @Test
    fun `mistakes flags 3x3 box conflicts`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.filled(2))
            .withCell(Coord(2, 2), Cell.filled(2))
        val state = GameUiState(puzzle = board, solution = Board.empty())
        val m = state.mistakes()
        assertTrue(Coord(0, 0) in m)
        assertTrue(Coord(2, 2) in m)
    }

    @Test
    fun `mistakes ignores empty cells`() {
        val board = Board.empty().withCell(Coord(0, 0), Cell.filled(5))
        val state = GameUiState(puzzle = board, solution = Board.empty())
        assertTrue(state.mistakes().isEmpty())
    }

    @Test
    fun `selectedDigit is null initially`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertNull(state.selectedDigit)
    }

    @Test
    fun `selectDigit with no cell selected just locks the digit`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val before = state.puzzle
        val after = state.selectDigit(5)
        assertEquals(5, after.selectedDigit)
        assertEquals(before, after.puzzle)
    }

    @Test
    fun `selectDigit with same digit toggles off`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val after = state.selectDigit(5).selectDigit(5)
        assertNull(after.selectedDigit)
    }

    @Test
    fun `selectDigit with cell selected places and locks`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val empty = firstEmptyCoord(state.puzzle)
        val after = state.selectCell(empty).selectDigit(7)
        assertEquals(7, after.puzzle.cell(empty).value)
        assertEquals(7, after.selectedDigit)
    }

    @Test
    fun `selectDigit switching to a different digit only relocks without placing`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val empty = firstEmptyCoord(state.puzzle)
        val after = state.selectDigit(3).selectCell(empty).selectDigit(8)
        assertEquals(3, after.puzzle.cell(empty).value)
        assertEquals(8, after.selectedDigit)
    }

    @Test
    fun `selectCell with locked digit places the digit at the new cell`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val empty = firstEmptyCoord(state.puzzle)
        val after = state.selectDigit(4).selectCell(empty)
        assertEquals(4, after.puzzle.cell(empty).value)
        assertEquals(4, after.selectedDigit)
        assertEquals(empty, after.selected)
    }

    @Test
    fun `selectCell with locked digit and noteMode places a note`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val empty = firstEmptyCoord(state.puzzle)
        val after = state.toggleNoteMode().selectDigit(2).selectCell(empty)
        assertTrue(2 in after.puzzle.cell(empty).notes)
        assertNull(after.puzzle.cell(empty).value)
        assertEquals(2, after.selectedDigit)
    }

    @Test
    fun `selectCell with locked digit on a given cell keeps digit locked and places nothing`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val given = firstGivenCoord(state.puzzle)
        val originalValue = state.puzzle.cell(given).value
        val after = state.selectDigit(4).selectCell(given)
        assertEquals(originalValue, after.puzzle.cell(given).value)
        assertEquals(4, after.selectedDigit)
        assertEquals(given, after.selected)
    }

    @Test
    fun `digitHighlights uses selectedDigit when set`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.filled(7))
            .withCell(Coord(3, 3), Cell.filled(7))
            .withCell(Coord(1, 1), Cell.filled(5))
        val state = GameUiState(puzzle = board, solution = Board.empty())
            .selectCell(Coord(1, 1))
            .selectDigit(7)
        // 5 was placed at (1,1) by selectDigit since it had a selected cell? No -
        // selectDigit(7) places 7 at the selected cell (1,1), overwriting 5.
        // The point: digitHighlights should use selectedDigit (7), not (1,1)'s value.
        val highlights = state.digitHighlights()
        assertTrue(Coord(0, 0) in highlights)
        assertTrue(Coord(3, 3) in highlights)
    }

    @Test
    fun `digitHighlights includes cells with selectedDigit in notes`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.empty().copy(notes = setOf(3, 7)))
            .withCell(Coord(5, 5), Cell.empty().copy(notes = setOf(7, 9)))
            .withCell(Coord(2, 2), Cell.empty().copy(notes = setOf(2, 4)))
        val state = GameUiState(puzzle = board, solution = Board.empty()).selectDigit(7)
        val highlights = state.digitHighlights()
        assertTrue(Coord(0, 0) in highlights)
        assertTrue(Coord(5, 5) in highlights)
        assertTrue(Coord(2, 2) !in highlights)
    }

    @Test
    fun `placing the 9th of a locked digit auto-unlocks the digit`() {
        var board = Board.empty()
        val placements = listOf(
            Coord(0, 0), Coord(1, 3), Coord(2, 6),
            Coord(3, 1), Coord(4, 4), Coord(5, 7),
            Coord(6, 2), Coord(7, 5),
        )
        for (coord in placements) board = board.withCell(coord, Cell.filled(7))
        val state = GameUiState(puzzle = board, solution = Board.empty())
            .selectDigit(7)
        val ninth = Coord(8, 8)
        val after = state.selectCell(ninth)
        assertEquals(7, after.puzzle.cell(ninth).value)
        assertNull(after.selectedDigit)
    }

    @Test
    fun `completedDigits is empty on a fresh puzzle`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertTrue(state.completedDigits().isEmpty())
    }

    @Test
    fun `completedDigits includes a digit when 9 are placed`() {
        var board = Board.empty()
        val placements = listOf(
            Coord(0, 0), Coord(1, 3), Coord(2, 6),
            Coord(3, 1), Coord(4, 4), Coord(5, 7),
            Coord(6, 2), Coord(7, 5), Coord(8, 8),
        )
        for (coord in placements) board = board.withCell(coord, Cell.filled(7))
        val state = GameUiState(puzzle = board, solution = Board.empty())
        assertTrue(7 in state.completedDigits())
    }

    @Test
    fun `completedDigits excludes a digit involved in a peer conflict`() {
        var board = Board.empty()
        for (col in 0..8) {
            board = board.withCell(Coord(0, col), Cell.filled(5))
        }
        val state = GameUiState(puzzle = board, solution = Board.empty())
        assertTrue(5 !in state.completedDigits())
    }

    @Test
    fun `digitHighlights is empty when nothing is selected`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertTrue(state.digitHighlights().isEmpty())
    }

    @Test
    fun `digitHighlights is empty when the selected cell has no value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val empty = firstEmptyCoord(state.puzzle)
        assertTrue(state.selectCell(empty).digitHighlights().isEmpty())
    }

    @Test
    fun `digitHighlights returns every coord with the same value`() {
        val board = Board.empty()
            .withCell(Coord(0, 0), Cell.filled(7))
            .withCell(Coord(3, 3), Cell.filled(7))
            .withCell(Coord(8, 8), Cell.filled(7))
            .withCell(Coord(1, 1), Cell.filled(2))
        val state = GameUiState(puzzle = board, solution = Board.empty())
            .selectCell(Coord(0, 0))
        val same = state.digitHighlights()
        assertEquals(setOf(Coord(0, 0), Coord(3, 3), Coord(8, 8)), same)
    }

    @Test
    fun `initial state has empty undo history`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertTrue(state.history.isEmpty())
        assertFalse(state.canUndo)
    }

    @Test
    fun `placeDigit pushes the previous board to history`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val after = state.selectCell(coord).placeDigit(7)
        assertEquals(1, after.history.size)
        assertTrue(after.canUndo)
    }

    @Test
    fun `undo restores the puzzle from the most recent snapshot`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val after = state.selectCell(coord).placeDigit(7).undo()
        assertNull(after.puzzle.cell(coord).value)
        assertEquals(0, after.history.size)
    }

    @Test
    fun `undo with empty history is a no-op`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertEquals(state, state.undo())
    }

    @Test
    fun `erase pushes history when it actually clears a cell`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val filled = state.selectCell(coord).placeDigit(7)
        val erased = filled.erase()
        assertEquals(2, erased.history.size)
        val undone = erased.undo()
        assertEquals(7, undone.puzzle.cell(coord).value)
    }

    @Test
    fun `no-op placeDigit on a given cell does not push history`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val givenCoord = firstGivenCoord(state.puzzle)
        val after = state.selectCell(givenCoord).placeDigit(3)
        assertEquals(0, after.history.size)
    }

    @Test
    fun `multiple undos walk back step by step`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord1 = firstEmptyCoord(state.puzzle)
        val s1 = state.selectCell(coord1).placeDigit(3)
        val coord2 = firstEmptyCoord(s1.puzzle)
        val s2 = s1.selectCell(coord2).placeDigit(5)

        val u1 = s2.undo()
        assertEquals(3, u1.puzzle.cell(coord1).value)
        assertNull(u1.puzzle.cell(coord2).value)

        val u2 = u1.undo()
        assertNull(u2.puzzle.cell(coord1).value)
    }

    @Test
    fun `undo restores the auto-removed peer notes`() {
        val board = Board.empty()
            .withCell(Coord(0, 5), Cell.empty().copy(notes = setOf(7)))
        val state = GameUiState(
            puzzle = board,
            solution = Board.empty(),
        )
        val after = state.selectCell(Coord(0, 0)).placeDigit(7).undo()
        assertTrue(7 in after.puzzle.cell(Coord(0, 5)).notes)
    }

    @Test
    fun `undo unsets isComplete after undoing the winning move`() {
        val puzzle = seededPuzzle()
        val almostSolved = buildAlmostSolvedState(puzzle, leaveEmpty = 1)
        val coord = firstEmptyCoord(almostSolved.puzzle)
        val correct = puzzle.solution.cell(coord).value!!
        val solved = almostSolved.selectCell(coord).placeDigit(correct)
        assertTrue(solved.isComplete)
        val undone = solved.undo()
        assertFalse(undone.isComplete)
    }

    @Test
    fun `placing a digit removes that digit from notes of row peers`() {
        val state = stateWithNotedPeers(
            notedCoord = Coord(0, 5),
            placeAt = Coord(0, 0),
            digit = 7,
        )
        val after = state.toggleNoteMode().toggleNoteMode() // ensure normal mode
            .selectCell(Coord(0, 0)).placeDigit(7)
        assertFalse(7 in after.puzzle.cell(Coord(0, 5)).notes)
    }

    @Test
    fun `placing a digit removes that digit from notes of column peers`() {
        val state = stateWithNotedPeers(
            notedCoord = Coord(5, 0),
            placeAt = Coord(0, 0),
            digit = 7,
        )
        val after = state.selectCell(Coord(0, 0)).placeDigit(7)
        assertFalse(7 in after.puzzle.cell(Coord(5, 0)).notes)
    }

    @Test
    fun `placing a digit removes that digit from notes of box peers`() {
        val state = stateWithNotedPeers(
            notedCoord = Coord(1, 1),
            placeAt = Coord(0, 0),
            digit = 7,
        )
        val after = state.selectCell(Coord(0, 0)).placeDigit(7)
        assertFalse(7 in after.puzzle.cell(Coord(1, 1)).notes)
    }

    @Test
    fun `placing a digit leaves notes in non-peer cells untouched`() {
        val state = stateWithNotedPeers(
            notedCoord = Coord(8, 8),
            placeAt = Coord(0, 0),
            digit = 7,
        )
        val after = state.selectCell(Coord(0, 0)).placeDigit(7)
        assertTrue(7 in after.puzzle.cell(Coord(8, 8)).notes)
    }

    @Test
    fun `placing a digit leaves other digits in peer notes untouched`() {
        val state = stateWithNotedPeers(
            notedCoord = Coord(0, 5),
            placeAt = Coord(0, 0),
            digit = 7,
            extraNotes = setOf(2, 4),
        )
        val after = state.selectCell(Coord(0, 0)).placeDigit(7)
        val notes = after.puzzle.cell(Coord(0, 5)).notes
        assertTrue(2 in notes)
        assertTrue(4 in notes)
    }

    private fun stateWithNotedPeers(
        notedCoord: Coord,
        placeAt: Coord,
        digit: Int,
        extraNotes: Set<Int> = emptySet(),
    ): GameUiState {
        var board = Board.empty()
        board = board.withCell(notedCoord, Cell.empty().copy(notes = setOf(digit) + extraNotes))
        return GameUiState(
            puzzle = board,
            solution = Board.empty(),
            selected = null,
            isComplete = false,
        )
    }

    @Test
    fun `noteMode is off initially`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertFalse(state.noteMode)
    }

    @Test
    fun `toggleNoteMode flips noteMode`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        assertTrue(state.toggleNoteMode().noteMode)
        assertFalse(state.toggleNoteMode().toggleNoteMode().noteMode)
    }

    @Test
    fun `placeDigit in noteMode adds the digit to notes`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val after = state.toggleNoteMode().selectCell(coord).placeDigit(3).placeDigit(7)
        assertEquals(setOf(3, 7), after.puzzle.cell(coord).notes)
        assertNull(after.puzzle.cell(coord).value)
    }

    @Test
    fun `placeDigit in noteMode toggles the digit out if already noted`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val after = state.toggleNoteMode().selectCell(coord)
            .placeDigit(3).placeDigit(7).placeDigit(3)
        assertEquals(setOf(7), after.puzzle.cell(coord).notes)
    }

    @Test
    fun `placeDigit in noteMode is a no-op on a given cell`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstGivenCoord(state.puzzle)
        val originalCell = state.puzzle.cell(coord)
        val after = state.toggleNoteMode().selectCell(coord).placeDigit(5)
        assertEquals(originalCell, after.puzzle.cell(coord))
    }

    @Test
    fun `placeDigit in noteMode is a no-op on a cell that already has a value`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val before = state.selectCell(coord).placeDigit(5)
        val after = before.toggleNoteMode().placeDigit(7)
        assertEquals(before.puzzle.cell(coord), after.puzzle.cell(coord))
    }

    @Test
    fun `erase in noteMode clears all notes`() {
        val state = GameUiState.fromPuzzle(seededPuzzle())
        val coord = firstEmptyCoord(state.puzzle)
        val withNotes = state.toggleNoteMode().selectCell(coord)
            .placeDigit(2).placeDigit(4).placeDigit(8)
        val after = withNotes.erase()
        assertTrue(after.puzzle.cell(coord).notes.isEmpty())
    }

    private fun firstGivenCoord(board: Board): Coord {
        for (r in 0..8) for (c in 0..8) {
            if (board.cell(Coord(r, c)).isGiven) return Coord(r, c)
        }
        error("no given cell")
    }

    private fun firstEmptyCoord(board: Board): Coord {
        for (r in 0..8) for (c in 0..8) {
            if (board.cell(Coord(r, c)).isEmpty) return Coord(r, c)
        }
        error("no empty cell")
    }

    private fun buildAlmostSolvedState(puzzle: Puzzle, leaveEmpty: Int): GameUiState {
        var board = puzzle.puzzle
        var left = leaveEmpty
        val emptyCoords = mutableListOf<Coord>()
        for (r in 0..8) for (c in 0..8) {
            val coord = Coord(r, c)
            if (board.cell(coord).isEmpty) emptyCoords += coord
        }
        val toFill = emptyCoords.dropLast(left)
        for (coord in toFill) {
            val v = puzzle.solution.cell(coord).value!!
            board = board.withCell(coord, Cell.filled(v))
        }
        return GameUiState(
            puzzle = board,
            solution = puzzle.solution,
            selected = null,
            isComplete = false,
        )
    }
}
