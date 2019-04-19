import { GridReadyEvent } from 'ag-grid-community';
import 'ag-grid-community/dist/styles/ag-grid.css';
import 'ag-grid-community/dist/styles/ag-theme-material.css';
import { AgGridReact } from 'ag-grid-react';
import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { DeployManager } from 'components/Deploy';
import loader from 'components/images/loader.gif';
import { FilterContextPool } from 'models/FilterContextPool';
import { PropertyType } from 'models/Filters';
import ModelRecord from 'models/ModelRecord';
import routes, { GetRouteParams } from 'routes';
import { IColumnMetaData } from 'store/dashboard-config';
import { checkDeployStatusForModelsIfNeed } from 'store/deploy';
import { fetchExperimentRuns } from 'store/experiment-runs';
import { IApplicationState, IConnectedReduxProps } from 'store/store';
import {
  defaultColDefinitions,
  returnColumnDefs,
} from './columnDefinitions/Definitions';
import DashboardConfig from './DashboardConfig/DashboardConfig';
import styles from './ExperimentRuns.module.css';

type IUrlProps = GetRouteParams<typeof routes.experimentRuns>;

interface IPropsFromState {
  data?: ModelRecord[] | undefined;
  loading: boolean;
  defaultColDefinitions: any;
  filterState: { [index: string]: {} };
  filtered: boolean;
  columnConfig: Map<string, IColumnMetaData>;
}

interface IOperator {
  '>': any;
  '<': any;
  [key: string]: any;
}

type AllProps = RouteComponentProps<IUrlProps> &
  IPropsFromState &
  IConnectedReduxProps;

class ExperimentRuns extends React.Component<AllProps> {
  public gridApi: any;
  public columnApi: any;
  public data: any;

  public callFilterUpdate = () => {
    this.gridApi.onFilterChanged();
  };

  public componentWillReceiveProps(nextProps: AllProps) {
    if (this.props !== nextProps && this.gridApi !== undefined) {
      setTimeout(this.callFilterUpdate, 1000);
    }

    if (this.gridApi && this.props.columnConfig !== nextProps.columnConfig) {
      this.gridApi.setColumnDefs(returnColumnDefs(nextProps.columnConfig));
      const el = document.getElementsByClassName('ag-center-cols-viewport');
      if (el !== undefined && el[0] !== undefined) {
        el[0].scrollLeft += 300;
      }
    }
  }

  public componentDidUpdate(prevProps: AllProps) {
    if (this.gridApi && this.props.data && prevProps.data !== this.props.data) {
      this.gridApi.setRowData(this.props.data);
    }
    if (this.props.data && prevProps.data !== this.props.data) {
      this.props.dispatch(
        checkDeployStatusForModelsIfNeed(this.props.data.map(({ id }) => id))
      );
    }
  }

  public render() {
    const { data, loading, columnConfig } = this.props;
    return loading ? (
      <img src={loader} className={styles.loader} />
    ) : data ? (
      <React.Fragment>
        <DashboardConfig />
        <DeployManager />
        <div className={`ag-theme-material ${styles.aggrid_wrapper}`}>
          <AgGridReact
            reactNext={true}
            pagination={true}
            onGridReady={this.onGridReady}
            animateRows={true}
            getRowHeight={this.gridRowHeight}
            columnDefs={returnColumnDefs(columnConfig)}
            rowData={undefined}
            domLayout="autoHeight"
            defaultColDef={this.props.defaultColDefinitions}
            isExternalFilterPresent={this.isExternalFilterPresent}
            doesExternalFilterPass={this.doesExternalFilterPass}
          />
        </div>
      </React.Fragment>
    ) : (
      ''
    );
  }

  public onGridReady = (event: GridReadyEvent) => {
    this.gridApi = event.api;
    this.columnApi = event.columnApi;
    this.gridApi.setRowData(this.props.data);
  };

  public gridRowHeight = (params: any) => {
    try {
      const data = params.node.data;
      if (
        (data.metrics && data.metrics.length > 3) ||
        (data.hyperparameters && data.hyperparameters.length > 3)
      ) {
        if (data.metrics && data.metrics.length > data.hyperparameters.length) {
          return (data.metric.length - 3) * 5 + 220;
        }
        return data.hyperparameters.length * 5 + 220;
      }
      if (data.tags && data.tags.length >= 6) {
        return 240;
      }
    } catch {}

    return 200;
  };

  public isExternalFilterPresent = () => {
    return this.props.filtered;
  };

  public funEvaluate(filter: any) {
    // this.data is from the bind(node) where node is table row data
    // **ts forced creation of public data var to be able access node
    const operators: IOperator = {
      '<': (a: number, b: number) => a < b,
      '>': (a: number, b: number) => a > b,
    };
    switch (filter.type) {
      case 'tag':
        return this.data.tags.includes(filter.key);
      case 'param':
        return this.data[filter.subtype].find(
          (params: any) => params.key === filter.param
        )
          ? operators[filter.operator](
              Number(
                this.data[filter.subtype].find((params: any) => {
                  if (params.key === filter.param) {
                    return params.value;
                  }
                }).value
              ),
              Number(filter.value)
            )
          : false;
      default:
        return true;
    }
  }

  public doesExternalFilterPass = (node: any) => {
    return Object.values(this.props.filterState)
      .map(this.funEvaluate.bind(node))
      .every(val => val === true);
  };
}

// filterState and filtered should be provided by from IApplicationState -> customFilter
const mapStateToProps = ({
  experimentRuns,
  dashboardConfig,
}: IApplicationState) => ({
  defaultColDefinitions,
  columnConfig: dashboardConfig.columnConfig,
  data: experimentRuns.data,
  loading: experimentRuns.loading,
  filterState: {},
  filtered: false,
});

export default connect(mapStateToProps)(ExperimentRuns);