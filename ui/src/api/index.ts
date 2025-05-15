import { axiosInstance } from "@halo-dev/api-client";
import {
  HandsomeIndexNowLogsV1alpha1Api,
} from "./generated";

const indexNowApiClient = {
  logs: new HandsomeIndexNowLogsV1alpha1Api(undefined, "", axiosInstance),
}

export { indexNowApiClient }; 