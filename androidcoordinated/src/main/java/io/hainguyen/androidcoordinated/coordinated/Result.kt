package org.de_studio.diary.base.architecture

/**
 * Created by HaiNguyen on 8/5/17.
 */
open class Result
open class ErrorResult(val error: Throwable): Result()
open class SuccessResult(): Result()
object ToRenderContent: Result()
object ToFinishView: Result()
object EmptyResult: Result()
