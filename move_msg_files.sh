#!/bin/bash
set -e

cd /Users/holmofy/chuyuwater/auth-center/api/src/main/java/com/hbcy/authcenter/api/modules/minor/msg

echo "=== Copy model files ==="
cp channel/model/MsgChannel.java model/
cp template/model/MsgTemplate.java model/
cp template/model/MsgTemplateContent.java model/
cp scheme/model/MsgScheme.java model/
cp scheme/model/MsgSchemeChannel.java model/
cp log/model/MsgLog.java model/
cp rule/model/SelectRule.java model/
cp rule/model/SelectRuleCondition.java model/

echo "=== Copy dao files ==="
cp channel/dao/MsgChannelMapper.java dao/
cp template/dao/MsgTemplateMapper.java dao/
cp template/dao/MsgTemplateContentMapper.java dao/
cp scheme/dao/MsgSchemeMapper.java dao/
cp scheme/dao/MsgSchemeChannelMapper.java dao/
cp log/dao/MsgLogMapper.java dao/
cp rule/dao/SelectRuleMapper.java dao/
cp rule/dao/SelectRuleConditionMapper.java dao/

echo "=== Copy vo files ==="
cp channel/vo/ChannelQueryVO.java vo/
cp channel/vo/ChannelCreateVO.java vo/
cp channel/vo/ChannelUpdateVO.java vo/
cp template/vo/TemplateQueryVO.java vo/
cp template/vo/TemplateCreateVO.java vo/
cp template/vo/TemplateUpdateVO.java vo/
cp template/vo/TemplateContentVO.java vo/
cp template/vo/TemplateTestVO.java vo/
cp scheme/vo/SchemeQueryVO.java vo/
cp scheme/vo/SchemeCreateVO.java vo/
cp scheme/vo/SchemeUpdateVO.java vo/
cp log/vo/MsgLogQueryVO.java vo/
cp rule/vo/RuleQueryVO.java vo/
cp rule/vo/RuleCreateVO.java vo/
cp rule/vo/RuleConditionVO.java vo/

echo "=== Copy service files ==="
cp channel/service/MsgChannelService.java service/
cp template/service/MsgTemplateService.java service/
cp scheme/service/MsgSchemeService.java service/
cp log/service/MsgLogService.java service/
cp rule/service/SelectRuleService.java service/

echo "=== Copy controller files ==="
cp channel/controller/MsgChannelController.java controller/
cp template/controller/MsgTemplateController.java controller/
cp scheme/controller/MsgSchemeController.java controller/
cp log/controller/MsgLogController.java controller/
cp rule/controller/SelectRuleController.java controller/

echo "=== Done copying ==="
