package football;

import football.cocos.FootballSiteCoCos;
import football.footballsite._ast.ASTFootballSite;
import football.footballsite._cocos.FootballSiteCoCoChecker;
import football.footballsite._parser.FootballSiteParser;
import football.generator.FootballSiteGenerator;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MCFootball CLI tool — main entry point.
 * <p>
 * Usage (production — single combined model, fail-fast):
 *   FootballSiteTool --model AllEurope.fb --output outputDir
 * <p>
 * Usage (dev — multiple models or directory scan, warn-and-continue):
 *   FootballSiteTool --models file1.fb file2.fb --output outputDir
 *   FootballSiteTool --models-dir dir/          --output outputDir
 * <p>
 * Only one of --model, --models, --models-dir may be specified.
 */
public class FootballSiteTool {

    public static void main(String[] args) {
        // Disable MontiCore's fail-quick so all CoCo errors are collected
        Log.enableFailQuick(false);

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        // ── Parse CLI arguments ──────────────────────────────────────
        String singleModel = null;      // --model (production)
        List<String> modelPaths = new ArrayList<>();  // --models (dev)
        String modelsDir = null;        // --models-dir (dev)
        String outputDir = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--model":
                    if (i + 1 < args.length) {
                        singleModel = args[++i];
                    }
                    break;
                case "--models":
                    // Collect all following args until next flag or end
                    i++;
                    while (i < args.length && !args[i].startsWith("--")) {
                        modelPaths.add(args[i]);
                        i++;
                    }
                    i--; // back up so the outer loop's i++ lands on the flag
                    break;
                case "--models-dir":
                    if (i + 1 < args.length) {
                        modelsDir = args[++i];
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        outputDir = args[++i];
                    }
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsage();
                    System.exit(1);
                    return;
            }
        }

        // ── Validate: exactly one model source flag ──────────────────
        int modeCount = (singleModel != null ? 1 : 0)
                      + (!modelPaths.isEmpty() ? 1 : 0)
                      + (modelsDir != null ? 1 : 0);

        if (modeCount == 0) {
            System.err.println("ERROR: One of --model, --models, or --models-dir is required.");
            printUsage();
            System.exit(1);
            return;
        }
        if (modeCount > 1) {
            System.err.println("ERROR: --model cannot be combined with --models or --models-dir. Use one mode.");
            System.exit(1);
            return;
        }
        if (outputDir == null) {
            System.err.println("ERROR: --output is required.");
            printUsage();
            System.exit(1);
            return;
        }

        // ── Production mode: --model (fail-fast) ─────────────────────
        if (singleModel != null) {
            runProductionMode(singleModel, outputDir);
            return;
        }

        // ── Dev mode: --models-dir → collect .fb paths ───────────────
        if (modelsDir != null) {
            File dir = new File(modelsDir);
            if (!dir.isDirectory()) {
                System.err.println("ERROR: --models-dir is not a directory: " + modelsDir);
                System.exit(1);
                return;
            }
            File[] fbFiles = dir.listFiles((d, name) -> name.endsWith(".fb"));
            if (fbFiles == null || fbFiles.length == 0) {
                System.err.println("WARNING: No .fb files found in " + modelsDir);
                return;
            }
            for (File f : fbFiles) {
                modelPaths.add(f.getAbsolutePath());
            }
            System.out.println("Found " + fbFiles.length + " .fb file(s) in " + modelsDir);
        }

        // ── Dev mode: parse each .fb (warn-and-continue) ─────────────
        runDevMode(modelPaths, outputDir);
    }

    /**
     * Production mode: parse exactly one .fb file.
     * Any error is fatal — prints message and exits with code 1.
     */
    private static void runProductionMode(String modelPath, String outputDir) {
        System.out.println("MCFootball Site Generator (production mode)");
        System.out.println("===========================================");
        System.out.println("Model: " + modelPath);

        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            System.err.println("ERROR: Model file not found: " + modelPath);
            System.exit(1);
            return;
        }

        try {
            FootballSiteParser parser = new FootballSiteParser();
            Optional<ASTFootballSite> optAst = parser.parseFootballSite(modelPath);

            if (!optAst.isPresent() || parser.hasErrors()) {
                System.err.println("ERROR: Failed to parse " + modelPath);
                System.exit(1);
                return;
            }

            ASTFootballSite ast = optAst.get();
            System.out.println("  Site name: " + ast.getName());
            System.out.println("  Countries: " + ast.getCountryList().size());

            // ── CoCo validation (fail-fast in production) ────────────
            long errorsBefore = Log.getErrorCount();
            FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
            checker.checkAll(ast);
            if (Log.getErrorCount() > errorsBefore) {
                System.err.println("ERROR: CoCo violations in " + modelPath
                        + " — aborting (production mode).");
                System.exit(1);
                return;
            }
            System.out.println("  CoCos: all checks passed");

            Path outPath = Paths.get(outputDir);
            FootballSiteGenerator generator = new FootballSiteGenerator(outPath);
            generator.generate(ast);

        } catch (IOException e) {
            System.err.println("ERROR reading " + modelPath + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        System.out.println();
        System.out.println("Done. Output in: " + outputDir);
    }

    /**
     * Dev mode: parse each .fb file, warn on errors but continue.
     */
    private static void runDevMode(List<String> modelPaths, String outputDir) {
        System.out.println("MCFootball Site Generator (dev mode)");
        System.out.println("=====================================");

        Path outPath = Paths.get(outputDir);
        FootballSiteGenerator generator = new FootballSiteGenerator(outPath);

        for (String modelPath : modelPaths) {
            System.out.println();
            System.out.println("Parsing: " + modelPath);

            try {
                FootballSiteParser parser = new FootballSiteParser();
                Optional<ASTFootballSite> optAst =
                        parser.parseFootballSite(modelPath);

                if (!optAst.isPresent()) {
                    System.err.println("ERROR: Failed to parse " + modelPath);
                    continue;
                }

                if (parser.hasErrors()) {
                    System.err.println("ERROR: Parse errors in " + modelPath);
                    continue;
                }

                ASTFootballSite ast = optAst.get();
                System.out.println("  Site name: " + ast.getName());
                System.out.println("  Countries: " + ast.getCountryList().size());

                // ── CoCo validation (warn-and-skip in dev mode) ──────
                long errorsBefore = Log.getErrorCount();
                FootballSiteCoCoChecker checker = FootballSiteCoCos.createChecker();
                checker.checkAll(ast);
                if (Log.getErrorCount() > errorsBefore) {
                    System.err.println("WARNING: CoCo violations in " + modelPath
                            + " — skipping generation for this model.");
                    continue;
                }
                System.out.println("  CoCos: all checks passed");

                generator.generate(ast);

            } catch (IOException e) {
                System.err.println("ERROR reading " + modelPath
                        + ": " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("Done. Output in: " + outputDir);
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  FootballSiteTool --model <file.fb>                   --output <dir>   (production)");
        System.out.println("  FootballSiteTool --models <file1.fb> [file2.fb ...]   --output <dir>   (dev)");
        System.out.println("  FootballSiteTool --models-dir <directory>             --output <dir>   (dev)");
        System.out.println();
        System.out.println("  --model       Single .fb model file (production mode, fail-fast on error)");
        System.out.println("  --models      One or more .fb model files (dev mode, warn-and-continue)");
        System.out.println("  --models-dir  Directory containing .fb files (dev mode, warn-and-continue)");
        System.out.println("  --output      Directory where HTML files will be generated");
        System.out.println();
        System.out.println("Only one of --model, --models, --models-dir may be specified.");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  FootballSiteTool --model AllEurope.fb --output output/");
        System.out.println("  FootballSiteTool --models Bundesliga.fb PremierLeague.fb --output output/");
        System.out.println("  FootballSiteTool --models-dir models/generated/ --output output/");
    }
}
