package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.iam.domain.PolicyDocument;
import ai.claudecode.esgt3.iam.domain.PolicyRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 정책 디렉터리 polling - mtime 변경 감지 시 PolicyRegistry 갱신.
 *
 * <p>{@code FileSystemWatcher}(NIO {@code WatchService})는 Mac/Linux/Windows 호환 이슈로
 * 단순 mtime polling 채택. 폴링 주기 기본 1초 → DoD &lt; 5초 충족.
 */
@Slf4j
public class PolicyHotReloader {

    private final PolicyRegistry registry;
    private final PolicyYamlLoader loader;
    private final Path directory;
    private final long pollIntervalMs;
    private final Map<String, Long> mtimes = new HashMap<>();
    private ScheduledExecutorService scheduler;

    public PolicyHotReloader(PolicyRegistry registry, PolicyYamlLoader loader, Path directory, long pollIntervalMs) {
        this.registry = registry;
        this.loader = loader;
        this.directory = directory;
        this.pollIntervalMs = pollIntervalMs;
    }

    public synchronized void start() {
        if (scheduler != null) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "policy-hot-reloader");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::pollOnce, 0, pollIntervalMs, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void pollOnce() {
        try {
            if (!Files.isDirectory(directory)) return;
            boolean changed = false;
            Map<String, Long> snapshot = new HashMap<>();
            try (Stream<Path> stream = Files.list(directory)) {
                List<Path> files = stream
                    .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                    .toList();
                for (Path p : files) {
                    long mtime = Files.getLastModifiedTime(p).toMillis();
                    snapshot.put(p.toString(), mtime);
                    Long prev = mtimes.get(p.toString());
                    if (prev == null || prev < mtime) changed = true;
                }
            }
            for (String existing : mtimes.keySet()) {
                if (!snapshot.containsKey(existing)) {
                    changed = true;
                    break;
                }
            }
            if (changed) {
                List<PolicyDocument> docs = new ArrayList<>();
                for (String path : snapshot.keySet()) {
                    docs.add(loader.loadFromResource(new FileSystemResource(path)));
                }
                registry.replace(docs);
                mtimes.clear();
                mtimes.putAll(snapshot);
                log.info("정책 핫리로드 완료 - {}개 파일", docs.size());
            }
        } catch (IOException e) {
            log.warn("정책 디렉터리 polling 실패: {}", e.getMessage());
        }
    }
}
