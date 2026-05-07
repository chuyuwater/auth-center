#!/bin/bash
set -e

BASE="/Users/holmofy/chuyuwater/auth-center/api/src/main/java/com/hbcy/authcenter/api/modules/minor/msg"

echo "=== Fix model package names ==="
for f in $BASE/model/MsgChannel.java $BASE/model/MsgTemplate.java $BASE/model/MsgTemplateContent.java $BASE/model/MsgScheme.java $BASE/model/MsgSchemeChannel.java $BASE/model/MsgLog.java $BASE/model/SelectRule.java $BASE/model/SelectRuleCondition.java; do
  sed -i '' 's/\.channel\.model/.model/g; s/\.template\.model/.model/g; s/\.scheme\.model/.model/g; s/\.log\.model/.model/g; s/\.rule\.model/.model/g' "$f"
done

echo "=== Fix dao package names ==="
for f in $BASE/dao/MsgChannelMapper.java $BASE/dao/MsgTemplateMapper.java $BASE/dao/MsgTemplateContentMapper.java $BASE/dao/MsgSchemeMapper.java $BASE/dao/MsgSchemeChannelMapper.java $BASE/dao/MsgLogMapper.java $BASE/dao/SelectRuleMapper.java $BASE/dao/SelectRuleConditionMapper.java; do
  sed -i '' 's/\.channel\.dao/.dao/g; s/\.template\.dao/.dao/g; s/\.scheme\.dao/.dao/g; s/\.log\.dao/.dao/g; s/\.rule\.dao/.dao/g; s/\.channel\.model/.model/g; s/\.template\.model/.model/g; s/\.scheme\.model/.model/g; s/\.log\.model/.model/g; s/\.rule\.model/.model/g' "$f"
done

echo "=== Fix vo package names ==="
for f in $BASE/vo/Channel*.java $BASE/vo/Template*.java $BASE/vo/Scheme*.java $BASE/vo/MsgLog*.java $BASE/vo/Rule*.java; do
  sed -i '' 's/\.channel\.vo/.vo/g; s/\.template\.vo/.vo/g; s/\.scheme\.vo/.vo/g; s/\.log\.vo/.vo/g; s/\.rule\.vo/.vo/g' "$f"
done

echo "=== Fix service package names ==="
for f in $BASE/service/MsgChannelService.java $BASE/service/MsgTemplateService.java $BASE/service/MsgSchemeService.java $BASE/service/MsgLogService.java $BASE/service/SelectRuleService.java; do
  sed -i '' 's/\.channel\.service/.service/g; s/\.template\.service/.service/g; s/\.scheme\.service/.service/g; s/\.log\.service/.service/g; s/\.rule\.service/.service/g' "$f"
  sed -i '' 's/\.channel\.model/.model/g; s/\.template\.model/.model/g; s/\.scheme\.model/.model/g; s/\.log\.model/.model/g; s/\.rule\.model/.model/g' "$f"
  sed -i '' 's/\.channel\.dao/.dao/g; s/\.template\.dao/.dao/g; s/\.scheme\.dao/.dao/g; s/\.log\.dao/.dao/g; s/\.rule\.dao/.dao/g' "$f"
  sed -i '' 's/\.channel\.vo/.vo/g; s/\.template\.vo/.vo/g; s/\.scheme\.vo/.vo/g; s/\.log\.vo/.vo/g; s/\.rule\.vo/.vo/g' "$f"
  sed -i '' 's/\.template\.dto/.dto/g; s/\.scheme\.dto/.dto/g; s/\.log\.dto/.dto/g; s/\.rule\.dto/.dto/g' "$f"
  sed -i '' 's/\.channel\.engine/.engine/g; s/\.template\.engine/.engine/g; s/\.scheme\.engine/.engine/g; s/\.log\.engine/.engine/g; s/\.rule\.engine/.engine/g' "$f"
done

echo "=== Fix controller package names ==="
for f in $BASE/controller/MsgChannelController.java $BASE/controller/MsgTemplateController.java $BASE/controller/MsgSchemeController.java $BASE/controller/MsgLogController.java $BASE/controller/SelectRuleController.java; do
  sed -i '' 's/\.channel\.controller/.controller/g; s/\.template\.controller/.controller/g; s/\.scheme\.controller/.controller/g; s/\.log\.controller/.controller/g; s/\.rule\.controller/.controller/g' "$f"
  sed -i '' 's/\.channel\.model/.model/g; s/\.template\.model/.model/g; s/\.scheme\.model/.model/g; s/\.log\.model/.model/g; s/\.rule\.model/.model/g' "$f"
  sed -i '' 's/\.channel\.service/.service/g; s/\.template\.service/.service/g; s/\.scheme\.service/.service/g; s/\.log\.service/.service/g; s/\.rule\.service/.service/g' "$f"
  sed -i '' 's/\.channel\.vo/.vo/g; s/\.template\.vo/.vo/g; s/\.scheme\.vo/.vo/g; s/\.log\.vo/.vo/g; s/\.rule\.vo/.vo/g' "$f"
  sed -i '' 's/\.channel\.dto/.dto/g; s/\.template\.dto/.dto/g; s/\.scheme\.dto/.dto/g; s/\.log\.dto/.dto/g; s/\.rule\.dto/.dto/g' "$f"
done

echo "=== Done ==="
