import {capitalize} from "@material-ui/core/utils/helpers";
import MUIDataTable, {IMUIDataTableOptions, IMuiDatatablesTableState} from "mui-datatables";
import * as React from "react";
import Event from "../../model/event/Event";
import EventPage from "../../model/event/EventPage";
import {EventType} from "../../model/event/EventType";


export default class HistoryPage extends React.Component<IHistoryPageProps, IHistoryPageState> {

    private columns = new Event().getKeys().map(k => this.keyToColumn(k))

    constructor(props: IHistoryPageProps) {
        super(props);
        this.showErrorInTable = this.showErrorInTable.bind(this);
        this.getOptions = this.getOptions.bind(this);
        this.state = {events: [], page: 0, count: 0, rowsPerPage: 10, serverSide: true};
    }

    public componentDidMount() {
        this.fetchHistoryPage(this.state);
    }

    public componentWillReceiveProps(nextProps: Readonly<IHistoryPageProps>, nextContext: any): void {
        if (this.state.page === 0 || !this.state.serverSide) {
            this.setState({
                count: this.state.count + 1,
                events: [nextProps.newEvent!, ...this.state.events]
            })
        }
    }

    public render() {
        return (
            <div className={"history-container"}>
                <MUIDataTable
                    title={"History"}
                    data={this.eventsToData()}
                    columns={this.columns}
                    options={this.getOptions()}
                />
            </div>
        );
    }

    private getOptions(): IMUIDataTableOptions {
        const defaultProps = {
            count: this.state.count,
            filterType: 'checkbox',
            page: this.state.page,
            rowsPerPage: this.state.rowsPerPage,
            rowsPerPageOptions: [10, 30, 50],
            serverSide: this.state.serverSide
        };
        return this.state.serverSide ? {
            ...defaultProps,
            onColumnSortChange: () => {
                this.switchToClientSide();
            },
            onFilterChange: () => {
                this.switchToClientSide();
            },
            onSearchChange: () => {
                this.switchToClientSide();
            },
            onTableChange: (action, tableState) => {
                if ("changeRowsPerPage" === action || "changePage" === action) {
                    this.fetchHistoryPage(tableState);
                }
            }
        } : defaultProps
    }

    private switchToClientSide() {
        this.setState({serverSide: false});
        this.fetchWholeHistory()
    }

    private fetchHistoryPage(tableState: IMuiDatatablesTableState) {
        this.props.getHistoryPage(tableState.rowsPerPage, tableState.page)
            .then(eventsPage => this.setState({
                count: eventsPage.totalElements,
                events: eventsPage.content,
                page: tableState.page,
                rowsPerPage: tableState.rowsPerPage
            }))
            .catch(error => {
                this.showErrorInTable(error);
            });
    }

    private fetchWholeHistory() {
        this.props.getHistory()
            .then(events => this.setState({
                count: events.length,
                events
            }))
            .catch(error => {
                this.showErrorInTable(error);
            });
    }

    private showErrorInTable(error: any) {
        const event = new Event();
        event.type = EventType.ERROR;
        event.title = "History API error";
        event.description = error.toString();
        event.time = new Date();
        this.setState({
            count: 1,
            events: [event],
            page: 0
        })
    }

    private eventsToData() {
        return this.state.events.map(e => this.eventToValues(e))
    }

    private eventToValues(event: Event) {
        return this.columns.map(c => (event as any)[c.key].toString())
    }

    private keyToColumn(key: string) {
        return {
            key,
            name: capitalize(key),
            options: {
                filter: key.toLowerCase() === "type",
                sort: true
            }
        }
    }
}

export interface IHistoryPageProps {
    newEvent?: Event;

    getHistoryPage(size: number, page: number): Promise<EventPage>;

    getHistory(): Promise<Event[]>;
}

interface IHistoryPageState extends IMuiDatatablesTableState {
    events: Event[];
    count: number;
    serverSide: boolean;
}