/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wikift.server.controller;

import com.wikift.common.enums.MessageEnums;
import com.wikift.common.enums.OrderEnums;
import com.wikift.common.utils.MessageUtils;
import com.wikift.common.utils.PageAndSortUtils;
import com.wikift.job.async.RamindAsyncJob;
import com.wikift.model.article.ArticleEntity;
import com.wikift.model.result.CommonResult;
import com.wikift.server.param.ArticleFabulousParam;
import com.wikift.server.param.ArticleViewParam;
import com.wikift.support.service.article.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "${wikift.api.path}")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private RamindAsyncJob ramindAsyncJob;

    @RequestMapping(value = "public/article/info/{id}", method = RequestMethod.GET)
    CommonResult<ArticleEntity> getArticle(@PathVariable(value = "id") Long id) {
        Assert.notNull(id, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.getArticle(id));
    }

    @RequestMapping(value = "public/article/list", method = RequestMethod.GET)
    CommonResult<ArticleEntity> list(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                     @RequestParam(value = "orderBy", defaultValue = "NATIVE_CREATE_TIME") OrderEnums order) {
        Assert.notNull(page, MessageUtils.getParamNotNull("page"));
        Assert.notNull(size, MessageUtils.getParamNotNull("size"));
        return CommonResult.success(articleService.findAll(order, PageAndSortUtils.getPage(page, size)));
    }

    @RequestMapping(value = "public/article/list/tag/{tagId}", method = RequestMethod.GET)
    CommonResult<ArticleEntity> getListByTag(@PathVariable(value = "tagId") Long tagId,
                                             @RequestParam(value = "page", defaultValue = "0") Integer page,
                                             @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Assert.notNull(page, MessageUtils.getParamNotNull("page"));
        Assert.notNull(size, MessageUtils.getParamNotNull("size"));
        Assert.notNull(tagId, MessageUtils.getParamNotNull("tagId"));
        return CommonResult.success(articleService.getAllByTagAndCreateTime(tagId, PageAndSortUtils.getPage(page, size)));
    }

    @RequestMapping(value = "public/article/view", method = RequestMethod.POST)
    CommonResult viewArticle(@RequestBody ArticleViewParam param) {
        Assert.notNull(param, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.viewArticle(param.getUserId(),
                param.getArticleId(),
                param.getViewCount(),
                param.getDevice()));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/create", method = RequestMethod.POST)
    CommonResult<ArticleEntity> createArticle(@RequestBody @Validated ArticleEntity entity) {
        Assert.notNull(entity, MessageEnums.PARAMS_NOT_NULL.getValue());
        entity.setId(0l);
        ArticleEntity article = articleService.save(entity);
        // 通知发送消息
        ramindAsyncJob.sendRamindToUserFollows(article);
        return CommonResult.success(article);
    }

    @PreAuthorize("hasPermission(#entity.id, 'update|article')")
    @RequestMapping(value = "article/update", method = RequestMethod.PUT)
    CommonResult<ArticleEntity> update(@RequestBody @Validated ArticleEntity entity) {
        Assert.notNull(entity, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.update(entity));
    }

    @PreAuthorize("hasPermission(#id, 'update|delete')")
    @RequestMapping(value = "article/delete/{id}", method = RequestMethod.DELETE)
    CommonResult<ArticleEntity> delete(@PathVariable(value = "id") Long id) {
        Assert.notNull(id, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.delete(id));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/my", method = RequestMethod.GET)
    CommonResult<ArticleEntity> getAllArticleByUser(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                    @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                    @RequestParam(value = "userId") Long userId) {
        Assert.notNull(page, MessageUtils.getParamNotNull("page"));
        Assert.notNull(page, MessageUtils.getParamNotNull("size"));
        Assert.notNull(userId, MessageUtils.getParamNotNull("userId"));
        return CommonResult.success(articleService.getMyArticles(userId, PageAndSortUtils.getPage(page, size)));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/top/by/user", method = RequestMethod.GET)
    CommonResult<ArticleEntity> findTopByUserEntityAndCreateTime(@RequestParam(value = "username") String username) {
        Assert.notNull(username, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.findTopByUserEntityAndCreateTime(username));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/fabulous", method = RequestMethod.POST)
    CommonResult fabulousArticle(@RequestBody ArticleFabulousParam param) {
        Assert.notNull(param, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.fabulousArticle(param.getUserId(), param.getArticleId()));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/unfabulous/{userId}/{articleId}", method = RequestMethod.DELETE)
    CommonResult unFabulousArticle(@PathVariable Integer userId,
                                   @PathVariable Integer articleId) {
        Assert.notNull(userId, MessageEnums.PARAMS_NOT_NULL.getValue());
        Assert.notNull(articleId, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.unFabulousArticle(userId, articleId));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/fabulous/check", method = RequestMethod.GET)
    CommonResult fabulousArticleCheck(@RequestParam(value = "userId") Integer userId,
                                      @RequestParam(value = "articleId") Integer articleId) {
        Assert.notNull(userId, MessageEnums.PARAMS_NOT_NULL.getValue());
        Assert.notNull(articleId, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.fabulousArticleExists(userId, articleId));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/fabulous/count", method = RequestMethod.GET)
    CommonResult fabulousArticleCount(@RequestParam(value = "articleId") Integer articleId) {
        Assert.notNull(articleId, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.fabulousArticleCount(articleId));
    }

    @PreAuthorize("hasAuthority(('USER'))")
    @RequestMapping(value = "article/view/count", method = RequestMethod.GET)
    CommonResult viewArticleCount(@RequestParam(value = "userId") Integer userId,
                                  @RequestParam(value = "articleId") Integer articleId) {
        Assert.notNull(userId, MessageEnums.PARAMS_NOT_NULL.getValue());
        Assert.notNull(articleId, MessageEnums.PARAMS_NOT_NULL.getValue());
        return CommonResult.success(articleService.viewArticleCount(userId, articleId));
    }

}
