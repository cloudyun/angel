/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */


package com.tencent.angel.spark.examples.cluster

import com.tencent.angel.ml.core.conf.{MLConf, SharedConf}
import com.tencent.angel.ml.core.utils.DataParser
import com.tencent.angel.ml.core.utils.paramsutils.JsonUtils
import com.tencent.angel.spark.context.PSContext
import com.tencent.angel.spark.examples.util.SparkUtils
import com.tencent.angel.spark.ml.core.{ArgsUtil, GraphModel, OfflineLearner}
import com.tencent.angel.spark.ml.util.Features
import org.apache.spark.{SparkConf, SparkContext}

object JsonRunner {
  def main(args: Array[String]): Unit = {
    val params    = ArgsUtil.parse(args)
    val input     = params.getOrElse("input", "")
    val output    = params.getOrElse("output", "")

    SharedConf.addMap(params)
    JsonUtils.init()

    val model     = new GraphModel
    val learner   = new OfflineLearner

    // load data
    val conf = new SparkConf()
    val sc   = new SparkContext(conf)
    val parser = DataParser(SharedConf.get())
    val data = sc.textFile(input)
      .repartition(SparkUtils.getNumExecutors(conf))
      .map(f => parser.parse(f))
    PSContext.getOrCreate(sc)

    val (matrixId, dim, newData) = Features.mapWithPS(data)
    SharedConf.get().setLong(MLConf.ML_FEATURE_INDEX_RANGE, dim)

    learner.train(newData, model)
    //    model.save(output)
  }

}