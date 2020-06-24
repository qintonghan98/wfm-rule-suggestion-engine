package src.main.java.com.googleintern.wfm.ruleengine.action;

import com.google.common.collect.ImmutableList;
import src.main.java.com.googleintern.wfm.ruleengine.model.UserPoolAssignmentModel;

/**
<<<<<<< HEAD
 * DataProcessor class is used to filter out invalid or conflict data.
=======
 * Data Processing class is used to filter out invalid or conflict data.
>>>>>>> c6e7411... WIP for code review comments.
 */
public class DataProcessor{

  /**
   * Filter out user data with invalid workgroup Id values(< 0).
   */
  public static ImmutableList<UserPoolAssignmentModel> filterValidData(
      ImmutableList<UserPoolAssignmentModel> rawData) {
    ImmutableList.Builder<UserPoolAssignmentModel> validDataBuilder =
        ImmutableList.<UserPoolAssignmentModel>builder();

    for (final UserPoolAssignmentModel data : rawData) {
      if (data.workgroupId() > 0) validDataBuilder.add(data);
    }
    return validDataBuilder.build();
  }
}
