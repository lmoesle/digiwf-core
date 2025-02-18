import {Ref} from "vue";
import {useQuery} from "@tanstack/vue-query";
import {Page} from "../commonModels";
import {callGetProcessInstances} from "../../api/processInstances/processInstancesApiCalls";
import {queryClient} from "../queryClient";
import {DateTime} from "luxon";
import {nullToUndefined} from "../../utils/dataTransformations";

export interface ProcessInstance { // FIXME: check nullable properties
  readonly id: string;
  readonly definitionName: string;
  readonly description?: string;
  readonly startTime?: string; // FIXME check date formation
  readonly endTime?: string;
  readonly status?: string;
}

const processInstancesQueryKey = "process-instances";
export const invalidProcessInstances = () =>
  queryClient.invalidateQueries([processInstancesQueryKey]);

export const useGetProcessInstances = (page: Ref<number>, size: Ref<number>, query: Ref<string | undefined>) =>
  useQuery({
    queryKey: [processInstancesQueryKey, page.value, size.value, query.value], //.filter(it => !!it), // remove query key if not set
    queryFn: () => {
      return callGetProcessInstances(page.value, size.value, nullToUndefined(query.value))
        .then(data => {

          const content = data.content?.map<ProcessInstance>(it => ({
            id: it.id || "", // FIXME: look if key could be really undefined
            definitionName: it.definitionName || "Unbekannt",
            description: it.description,
            startTime: it.startTime ? DateTime.fromISO(it.startTime).toLocaleString(DateTime.DATETIME_SHORT) : "-",
            endTime: it.endTime,
            status: it.status,
          })) ?? [];

          return Promise.resolve<Page<ProcessInstance>>({
            content,
            page: data.number || 0,
            size: data.size || 0,
            totalPages: data.totalPages || 0,
            totalElements: data.totalElements
          });
        });
    },
  });
