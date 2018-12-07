import Checkbox from "@material-ui/core/Checkbox/Checkbox";
import IconButton from "@material-ui/core/IconButton/IconButton";
import Tooltip from "@material-ui/core/Tooltip/Tooltip";
import {capitalize} from "@material-ui/core/utils/helpers";
import DeleteIcon from '@material-ui/icons/Delete';
import ReadIcon from '@material-ui/icons/Drafts';
import MUIDataTable, {IMUIDataTableColumn, IMUIDataTableOptions, IMuiDatatablesTableState} from "mui-datatables";
import * as React from "react";
import Event from "../../model/event/Event";
import EventPage from "../../model/event/EventPage";
import {EventType} from "../../model/event/EventType";


export default class HistoryPage extends React.PureComponent<IHistoryPageProps, IHistoryPageState> {

    private columns = new Event().getKeys().map(k => this.keyToColumn(k));

    constructor(props: IHistoryPageProps) {
        super(props);
        this.showErrorInTable = this.showErrorInTable.bind(this);
        this.getOptions = this.getOptions.bind(this);
        this.state = {events: [], page: 0, count: 0, rowsPerPage: 10, serverSide: true};
    }

    public componentDidMount() {
        window.document.title = "FCMS - History";
        this.fetchHistoryPage(this.state);
    }

    public componentWillReceiveProps(nextProps: Readonly<IHistoryPageProps>, nextContext: any): void {
        if (nextProps.newEvent !== undefined) {
            const changedIndex = this.state.events.findIndex(e => e.id === nextProps.newEvent!.id);
            if (changedIndex >= 0) {
                if (nextProps.newEvent.read !== this.state.events[changedIndex].read) {
                    const events = this.state.events.slice();
                    events.splice(changedIndex, 1, nextProps.newEvent!);
                    this.setState({events});
                }
            } else if ((this.state.page === 0 || !this.state.serverSide)) {
                this.setState({
                    count: this.state.count + 1,
                    events: [nextProps.newEvent, ...this.state.events]
                })
            }
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
        const defaultProps: IMUIDataTableOptions = {
            count: this.state.count,
            customToolbar: () => {
                return (
                    <React.Fragment>
                        <Tooltip title={"Delete all"} onClick={this.deleteAll} style={{marginLeft: 24}}>
                            <IconButton>
                                <DeleteIcon/>
                            </IconButton>
                        </Tooltip>
                        <Tooltip title={"Read all"} onClick={this.readAll}>
                            <IconButton>
                                <ReadIcon/>
                            </IconButton>
                        </Tooltip>
                    </React.Fragment>
                );
            },
            filterType: 'checkbox',
            page: this.state.page,
            rowsPerPage: this.state.rowsPerPage,
            rowsPerPageOptions: [10, 30, 50],
            selectableRows: false,
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
        event.id = "undefined";
        event.read = true;
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
        const column: IMUIHistoryTableColumn = {
            key,
            name: capitalize(key),
            options: {
                display: key.toLowerCase() === "id" ? "false" : "true",
                filter: key.toLowerCase() === "type",
                sort: true
            }
        };
        if (key.toLowerCase() === "read") {
            column.options!.customBodyRender = (value: string, tableMeta: any, updateValue: any) => {
                const read = value === 'true';
                const rowData = tableMeta.rowData;
                const onChange = (_: any) => {
                    if (!read) {
                        const event = this.state.events.find(e => e.id === rowData[0])!;
                        this.markAsRead({...event} as Event);
                    }
                };
                return (
                    <Checkbox
                        checked={read}
                        onChange={onChange}
                        disabled={read}
                    />
                )
            }
        }
        return column
    }

    private markAsRead = (event: Event) => {
        this.props.setEventAsRead(event)
            .then(r => {
                const changedIndex = this.state.events.findIndex(e => e.id === event.id);
                const events = this.state.events.slice();
                events.splice(changedIndex, 1, event);
                this.setState({events});
                this.props.onEventRead(event);
            })
            .catch(error => {
                this.showErrorInTable(error);
            });
    };

    private readAll = () => {
        this.props.setHistoryAsRead()
            .then(r => {
                const events = this.state.events.map(it => {
                    const event: Event = {...it} as Event;
                    event.read = true;
                    this.props.onEventRead(event, true);
                    return event;
                });
                this.setState({events})
            })
            .catch(error => {
                this.showErrorInTable(error);
            });
    };

    private deleteAll = () => {
        this.props.deleteHistory()
            .then(r => {
                this.state.events.forEach(it => {
                    const event: Event = {...it} as Event;
                    event.read = true;
                    this.props.onEventRead(event, true);
                });
                this.setState({events: []})
            })
            .catch(error => {
                this.showErrorInTable(error);
            });
    };
}

interface IMUIHistoryTableColumn extends IMUIDataTableColumn {
    key: string;
}

export interface IHistoryPageProps {
    newEvent?: Event;

    getHistoryPage(size: number, page: number): Promise<EventPage>;

    getHistory(): Promise<Event[]>;

    setEventAsRead(event: Event): Promise<Response>

    onEventRead(event: Event, all?: boolean): void

    deleteHistory(): Promise<Response>

    setHistoryAsRead(): Promise<Response>
}

interface IHistoryPageState extends IMuiDatatablesTableState {
    events: Event[];
    count: number;
    serverSide: boolean;
}