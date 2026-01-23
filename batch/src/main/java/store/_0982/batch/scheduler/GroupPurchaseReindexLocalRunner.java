//package store._0982.batch.scheduler;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//@Profile("dev")
//@Component
//@RequiredArgsConstructor
//public class GroupPurchaseReindexLocalRunner implements ApplicationRunner {
//
//    private final JobLauncher jobLauncher;
//    private final Job groupPurchaseReindexJob;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        jobLauncher.run(
//                groupPurchaseReindexJob,
//                new JobParametersBuilder()
//                        .addString("indexAlias", "group-purchase")
//                        .addString("mode", "full")
//                        .addLong("runId", System.currentTimeMillis())
//                        .toJobParameters()
//        );
//    }
//}
//
