package de.timmi6790.utility.utils;

import de.timmi6790.utility.ListenerComponent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Log4j2
public class TaskScheduler implements ListenerComponent {
    @Getter
    private static final TaskScheduler instance = new TaskScheduler();
    private final BlockingQueue<Task> taskList = new PriorityBlockingQueue<>(5, Comparator.comparingLong(Task::getNextRun));
    private long tick = 0;

    private TaskScheduler() {
        registerEvents();
    }

    public void schedule(final long delay, MinecraftTimeUnit timeUnit, Runnable runnable) {
        this.schedule(timeUnit.toTicks(delay), runnable);
    }

    public void schedule(final long delayInTicks, final Runnable runnable) {
        this.taskList.add(new Task(runnable, tick + delayInTicks));
    }

    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        tick++;

        // Run all tasks that are ready to be run
        Task task;
        while ((task = this.taskList.peek()) != null && tick >= task.getNextRun()) {
            log.debug("Running task {} during tick {}", task, tick);
            task.getRunnable().run();
            this.taskList.poll();
        }
    }

    @Getter(AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString
    private static class Task {
        private final Runnable runnable;
        private final long nextRun;
    }
}
