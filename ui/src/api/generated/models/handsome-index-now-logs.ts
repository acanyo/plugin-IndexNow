/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.15
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import type { IndexNowLogsSpec } from './index-now-logs-spec';
// May contain unused imports in some cases
// @ts-ignore
import type { Metadata } from './metadata';

/**
 * 
 * @export
 * @interface HandsomeIndexNowLogs
 */
export interface HandsomeIndexNowLogs {
    /**
     * 
     * @type {string}
     * @memberof HandsomeIndexNowLogs
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof HandsomeIndexNowLogs
     */
    'kind': string;
    /**
     * 
     * @type {IndexNowLogsSpec}
     * @memberof HandsomeIndexNowLogs
     */
    'logsSpec': IndexNowLogsSpec;
    /**
     * 
     * @type {Metadata}
     * @memberof HandsomeIndexNowLogs
     */
    'metadata': Metadata;
}

