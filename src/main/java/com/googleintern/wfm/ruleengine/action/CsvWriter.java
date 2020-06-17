package src.main.java.com.googleintern.wfm.ruleengine.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opencsv.CSVWriter;
import src.main.java.com.googleintern.wfm.ruleengine.model.FilterModel;
import src.main.java.com.googleintern.wfm.ruleengine.model.RuleModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/** CsvWriter class is used to write newly generated rules into a csv file. */
public class CsvWriter {
  enum Separator {
    SEMICOLON(";"),
    COMMA(","),
    SQUARE_BRACKET_LEFT("["),
    SQUARE_BRACKET_RIGHT("]"),
    CURLY_BRACKET_LEFT("{"),
    CURLY_BRACKET_RIGHT("}");

    final String symbol;

    Separator(String symbol) {
      this.symbol = symbol;
    }
  }

  private static final String SKILL_ID_FORMAT = "skill_id:";
  private static final String ROLE_ID_FORMAT = "role_id:";
  private static final String OUTPUT_CSV_FILE_NAME = "Generated Rules" + ".csv";
  private static final String[] CSV_FILE_HEADER =
      new String[] {
        "Workforce ID", "Workgroup ID", "Case Pool ID", "Permission Set IDs", "Filters"
      };

  /**
   * Create a new csv file using OUTPUT_CSV_FILE_NAME as name in outputCsvFileLocation and write
   * data into the newly created csv file.
   *
   * @throws IOException
   */
  public static void writeDataIntoCsvFile(String outputCsvFilePath, ImmutableList<RuleModel> rules)
      throws IOException {
    File outputFile = new File(outputCsvFilePath);
    Files.deleteIfExists(outputFile.toPath());
    outputFile.createNewFile();

    FileWriter outputFileWriter = new FileWriter(outputFile);
    CSVWriter csvWriter = new CSVWriter(outputFileWriter);

    csvWriter.writeNext(CSV_FILE_HEADER);
    ImmutableList<String[]> data =
        rules.stream().map(CsvWriter::writeData).collect(ImmutableList.toImmutableList());

    csvWriter.writeAll(data);
    csvWriter.close();
  }

  private static String[] writeData(final RuleModel rule) {
    String workforceId = Long.toString(rule.workforceId());
    String workgroupId = Long.toString(rule.workgroupId());
    String casePoolId = Long.toString(rule.casePoolId());
    String permissionIds = writePermissionSetIds(rule.permissionSetIds());
    String filterIds = writeFilterIds(rule.filters());
    return new String[] {workforceId, workgroupId, casePoolId, permissionIds, filterIds};
  }

  private static String writePermissionSetIds(ImmutableSet<Long> permissionSetIds) {
    String permissionIds = Separator.SQUARE_BRACKET_LEFT.symbol;
    for (final Long permissionSetId : permissionSetIds) {
      if (permissionIds.length() > 1) permissionIds = permissionIds + Separator.COMMA.symbol;
      permissionIds = permissionIds + permissionSetId;
    }
    permissionIds = permissionIds + Separator.SQUARE_BRACKET_RIGHT.symbol;
    return permissionIds;
  }

  private static String writeFilterIds(List<ImmutableSet<FilterModel>> filters) {
    String filterIds = Separator.SQUARE_BRACKET_LEFT.symbol;
    for (final ImmutableSet<FilterModel> filterSet : filters) {
      if (filterIds.length() > 1) filterIds = filterIds + Separator.SEMICOLON.symbol;
      String currFilterIds = "";
      for (final FilterModel filter : filterSet) {
        currFilterIds =
            currFilterIds.length() > 0
                ? currFilterIds + Separator.COMMA.symbol
                : currFilterIds + Separator.CURLY_BRACKET_LEFT.symbol;
        if (filter.type() == FilterModel.FilterType.SKILL
            || filter.type() == FilterModel.FilterType.ROLESKILL) {
          currFilterIds = currFilterIds + SKILL_ID_FORMAT;
        } else if (filter.type() == FilterModel.FilterType.ROLE) {
          currFilterIds = currFilterIds + ROLE_ID_FORMAT;
        }
        currFilterIds = currFilterIds + filter.value();
      }
      filterIds =
          currFilterIds.length() > 0
              ? filterIds + currFilterIds + Separator.CURLY_BRACKET_RIGHT.symbol
              : filterIds;
    }
    filterIds = filterIds + Separator.SQUARE_BRACKET_RIGHT.symbol;
    return filterIds;
  }
}