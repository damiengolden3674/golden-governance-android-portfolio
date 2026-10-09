package com.goldengovernance.habittracker
import org.junit.Assert.assertEquals
import org.junit.Test
class HabitMathTest {
 @Test fun percentageCapped(){assertEquals(100,completionPercent(40,21))}
 @Test fun zeroGoalSafe(){assertEquals(0,completionPercent(1,0))}
 @Test fun halfway(){assertEquals(50,completionPercent(5,10))}
}
