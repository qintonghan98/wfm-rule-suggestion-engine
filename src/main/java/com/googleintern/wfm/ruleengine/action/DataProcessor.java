package src.main.java.com.googleintern.wfm.ruleengine.action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import src.main.java.com.googleintern.wfm.ruleengine.model.FilterModel;
import src.main.java.com.googleintern.wfm.ruleengine.model.UserModel;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

/** DataProcessor class is used to filter out invalid or conflict data. */
public class DataProcessor {

  /** Filter out user data with invalid workgroup Id values(< 0). */
  public static ImmutableList<UserModel> filterValidData(
      ImmutableList<UserModel> rawData) {
    ImmutableList.Builder<UserModel> validDataBuilder =
        ImmutableList.<UserModel>builder();

    for (final UserModel data : rawData) {
      if (data.workgroupId() > 0) validDataBuilder.add(data);
    }
    return validDataBuilder.build();
  }

  public static void printConflictUserPairs(ImmutableSetMultimap<Long, Long> conflictUserPairs) {
    for (Long userId : conflictUserPairs.keySet()) {
      StringBuilder conflictRow = new StringBuilder();
      conflictRow.append(userId + "  [");
      ImmutableSet<Long> conflictUserIds = conflictUserPairs.get(userId);
      for (Long conflictId : conflictUserIds) {
        conflictRow.append(conflictId + "   ");
      }
      conflictRow.append("]");
      System.out.println(conflictRow.toString());
    }
  }

  public static ImmutableSet<UserModel> removeAllCoveredConflictUsers(
          ImmutableList<UserModel> users, ImmutableSet<Long> coveredConflictUsers) {
    return users.stream()
            .filter(user -> !coveredConflictUsers.contains(user.userId()))
            .collect(toImmutableSet());
  }

  public static ImmutableSet<Long> collectAllCoveredConflictUsers(
          ImmutableSetMultimap<Long, Long> conflictUserPairs) {
    ImmutableSet.Builder<Long> coveredConflictUsers = ImmutableSet.builder();
    conflictUserPairs
            .keySet()
            .forEach(key -> coveredConflictUsers.addAll(conflictUserPairs.get(key)));
    return coveredConflictUsers.build();
  }

  /**
   * key userId: filter{a, b, c}, permission{1, 2}
   * value userId: filter{a, b}, permission{1, 2, 3}
   */
  public static ImmutableSetMultimap<Long, Long> findConflictUserPairs(
          ImmutableList<UserModel> users) {
    ImmutableSetMultimap.Builder<Long, Long> conflictUserPairsBuilder =
            ImmutableSetMultimap.builder();
    for (int i = 0; i < users.size(); i++) {
      UserModel currentUser = users.get(i);
      ImmutableSet<FilterModel> currentFilters = convertSkillIdRoleIdToFilter(currentUser);
      for (int j = i + 1; j < users.size(); j++) {
        UserModel comparedUser = users.get(j);
        ImmutableSet<FilterModel> comparedFilters = convertSkillIdRoleIdToFilter(comparedUser);
        if (currentFilters.containsAll(comparedFilters)
                && !currentUser.poolAssignments().containsAll(comparedUser.poolAssignments())) {
          conflictUserPairsBuilder.put(currentUser.userId(), comparedUser.userId());
        }
        if (comparedFilters.containsAll(currentFilters)
                && !comparedUser.poolAssignments().containsAll(currentUser.poolAssignments())) {
          conflictUserPairsBuilder.put(comparedUser.userId(), currentUser.userId());
        }
      }
    }
    return conflictUserPairsBuilder.build();
  }

  private static ImmutableSet<FilterModel> convertSkillIdRoleIdToFilter(UserModel user) {
    ImmutableSet.Builder<FilterModel> filtersBuilder = ImmutableSet.builder();
    for (Long roleId : user.roleIds()) {
      filtersBuilder.add(
              FilterModel.builder().setType(FilterModel.FilterType.ROLE).setValue(roleId).build());
    }
    for (Long skillId : user.skillIds()) {
      filtersBuilder.add(
              FilterModel.builder().setType(FilterModel.FilterType.SKILL).setValue(skillId).build());
    }
    for (Long roleSkillId : user.roleSkillIds()) {
      filtersBuilder.add(
              FilterModel.builder()
                      .setType(FilterModel.FilterType.SKILL)
                      .setValue(roleSkillId)
                      .build());
    }
    return filtersBuilder.build();
  }
}
