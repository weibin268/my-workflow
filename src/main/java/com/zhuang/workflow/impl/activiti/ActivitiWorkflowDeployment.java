package com.zhuang.workflow.impl.activiti;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuang.workflow.WorkflowDeployment;
import com.zhuang.workflow.impl.activiti.manager.DeploymentManager;
import com.zhuang.workflow.model.PageInfo;
import com.zhuang.workflow.enums.DeploymentInfoNames;
import com.zhuang.workflow.model.DeploymentInfo;

public class ActivitiWorkflowDeployment implements WorkflowDeployment {

    @Autowired
    private DeploymentManager deploymentManager;

    @Autowired
    private RepositoryService repositoryService;

    public void deployByInputStream(String resourceName, InputStream inputStream) {
        deploymentManager.deployByInputStream(resourceName, inputStream);
    }

    public PageInfo<DeploymentInfo> getDeploymentInfoPage(int pageNo, int pageSize,
                                                          Map<String, Object> conditions) {

        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
        deploymentQuery.orderByDeploymenTime().desc();


        List<DeploymentInfo> deploymentInfoList = new ArrayList<DeploymentInfo>();

        PageInfo<DeploymentInfo> result = new PageInfo<DeploymentInfo>(pageNo, pageSize, new Long(deploymentQuery.count()).intValue(),
                deploymentInfoList);


        setDeploymentQueryConditions(deploymentQuery, conditions);


        //得到分页记录
        List<Deployment> deployments = deploymentQuery.listPage(result.getPageStartRow() - 1, result.getPageSize());

        for (Deployment deployment : deployments) {

            DeploymentInfo deploymentInfo = new DeploymentInfo();

            deploymentInfo.setDeployId(deployment.getId());
            deploymentInfo.setDeployName(deployment.getName());
            deploymentInfo.setDeployCategory(deployment.getCategory());
            deploymentInfo.setDeployTime(deployment.getDeploymentTime());


            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
            deploymentInfo.setProcDefKey(processDefinition.getKey());
            deploymentInfo.setProcDefName(processDefinition.getName());
            deploymentInfo.setProcDefVersion(processDefinition.getVersion());
            deploymentInfo.setProcDefDescription(processDefinition.getDescription());

            deploymentInfoList.add(deploymentInfo);
        }

        return result;
    }


    private void setDeploymentQueryConditions(DeploymentQuery deploymentQuery, Map<String, Object> conditions) {

        if (conditions != null) {

            if (conditions.containsKey(DeploymentInfoNames.PROC_DEF_KEY)) {
                Object objProcDefKey = conditions.get(DeploymentInfoNames.PROC_DEF_KEY);
                if (objProcDefKey != null && !objProcDefKey.toString().trim().isEmpty()) {
                    deploymentQuery.processDefinitionKey(objProcDefKey.toString().trim());
                }
            }

            if (conditions.containsKey(DeploymentInfoNames.DEPLOY_NAME)) {
                Object objDeployName = conditions.get(DeploymentInfoNames.DEPLOY_NAME);
                if (objDeployName != null && !objDeployName.toString().trim().isEmpty()) {
                    deploymentQuery.deploymentNameLike("%" + objDeployName.toString() + "%");
                }
            }

        }

    }

}
