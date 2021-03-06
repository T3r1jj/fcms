import AppBar, {AppBarProps} from '@material-ui/core/AppBar';
import Badge from '@material-ui/core/Badge';
import IconButton from '@material-ui/core/IconButton';
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import {StyleRulesCallback, withStyles} from '@material-ui/core/styles';
import {fade} from '@material-ui/core/styles/colorManipulator';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import CallbackIcon from '@material-ui/icons/CallMissed';
import HomeIcon from '@material-ui/icons/Home';
import MoreIcon from '@material-ui/icons/MoreVert';
import NotificationsIcon from '@material-ui/icons/Notifications';
import LogoutIcon from '@material-ui/icons/PowerSettingsNew';
import SearchIcon from '@material-ui/icons/Search';
import React from 'react';
import {Link} from "react-router-dom";
import {createFilter} from "react-select";
import Select from "react-select/lib/Select";
import {CellMeasurer, CellMeasurerCache, List, ListRowRenderer} from 'react-virtualized';
import SearchItem from "../model/SearchItem";

const styles: StyleRulesCallback = theme => ({
    grow: {
        flexGrow: 1,
    },
    inputInput: {
        paddingBottom: theme.spacing.unit,
        paddingLeft: theme.spacing.unit * 10,
        paddingRight: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        transition: theme.transitions.create('width'),
        width: '100%',
        [theme.breakpoints.up('md')]: {
            width: 200,
        },
    },
    inputRoot: {
        color: 'inherit',
        width: '100%',
    },
    linkWrapper: {
        color: 'inherit'
    },
    menuButton: {
        marginLeft: -12,
        marginRight: 20,
    },
    root: {
        width: '100%',
    },
    search: {
        '&:hover': {
            backgroundColor: fade(theme.palette.common.white, 0.25),
        },
        backgroundColor: fade(theme.palette.common.white, 0.15),
        borderRadius: theme.shape.borderRadius,
        marginLeft: 0,
        marginRight: theme.spacing.unit * 2,
        position: 'relative',
        width: '100%',
        [theme.breakpoints.up('sm')]: {
            marginLeft: theme.spacing.unit * 3,
            width: 'auto',
        },
    },
    searchIcon: {
        alignItems: 'center',
        display: 'flex',
        height: '100%',
        justifyContent: 'center',
        pointerEvents: 'none',
        position: 'absolute',
        right: 0,
        width: theme.spacing.unit * 4.5,
    },
    sectionDesktop: {
        display: 'none',
        [theme.breakpoints.up('md')]: {
            display: 'flex',
        },
    },
    sectionMobile: {
        display: 'flex',
        [theme.breakpoints.up('md')]: {
            display: 'none',
        },
    },
    title: {
        display: 'none',
        [theme.breakpoints.up('sm')]: {
            display: 'block',
        },
    },
});
const MenuList = (props: any) => {
    const rows = props.children;
    let rowCount: number;
    let height = 300;
    if (rows === undefined || rows.length === undefined) {
        rowCount = 0;
    } else {
        rowCount = rows.length;
    }
    if (rowCount < 10) {
        height = 0;
        for (let i = 0; i < rowCount; i++) {
            height += cache.rowHeight({index: i});
        }
    }

    const rowRenderer: ListRowRenderer = ({key, parent, index, isScrolling, isVisible, style}) => (
        <CellMeasurer
            cache={cache}
            columnIndex={0}
            key={key}
            parent={parent}
            rowIndex={index}
        >
            {({measure}) => (
                <div style={style}>
                    <div onLoad={measure}>{rows[index]}</div>
                </div>
            )}
        </CellMeasurer>
    );

    return (
        <List
            style={{width: '100%'}}
            width={250}
            height={height}
            rowCount={rowCount}
            rowRenderer={rowRenderer}
            deferredMeasurementCache={cache}
            rowHeight={cache.rowHeight}
        />
    )
};
const cache = new CellMeasurerCache({
    fixedHeight: false,
    fixedWidth: true,
});

class PrimarySearchAppBar extends React.PureComponent<IAppBarProps, IAppBarState> {

    constructor(props: IAppBarProps) {
        super(props);
        this.state = {
            inputValue: "",
            menuOpen: false,
            mobileMoreAnchorEl: null,
        };
    }

    public handleMobileMenuOpen = (event: any) => {
        this.setState({mobileMoreAnchorEl: event.currentTarget});
    };

    public handleMobileMenuClose = () => {
        this.setState({mobileMoreAnchorEl: null});
    };

    public render() {
        const mobileMoreAnchorEl = this.state.mobileMoreAnchorEl;
        const classes = this.props.classes;
        const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);
        const getClasses = () => {
            return classes as any
        };
        const notificationIcon = this.props.unreadCount === 0 ? <NotificationsIcon/> :
            <Badge badgeContent={this.props.unreadCount} color="secondary">
                <NotificationsIcon/>
            </Badge>

        const renderMobileMenu = (
            <Menu
                anchorEl={mobileMoreAnchorEl}
                anchorOrigin={{vertical: 'top', horizontal: 'right'}}
                transformOrigin={{vertical: 'top', horizontal: 'right'}}
                open={isMobileMenuOpen}
                onClose={this.handleMobileMenuClose}
            >
                <MenuItem>
                    <IconButton color="inherit">
                        <Link to={"/code"} className={getClasses().linkWrapper}>
                            <CallbackIcon/>
                        </Link>
                    </IconButton>
                    <p>Code callbacks</p>
                </MenuItem>
                <MenuItem>
                    <IconButton color="inherit">
                        <Link to={"/history"} className={getClasses().linkWrapper}>
                            {notificationIcon}
                        </Link>
                    </IconButton>
                    <p>Notifications</p>
                </MenuItem>
                <MenuItem onClick={this.logout}>
                    <IconButton color="inherit">
                        <LogoutIcon/>
                    </IconButton>
                    <p>Logout</p>
                </MenuItem>
            </Menu>
        );

        return (
            <div className={getClasses().root}>
                <AppBar position="static">
                    <Toolbar>
                        <IconButton className={getClasses().menuButton} color="inherit" aria-label="Home">
                            <Link to={"/"} className={getClasses().linkWrapper}>
                                <HomeIcon/>
                            </Link>
                        </IconButton>
                        <Typography className={getClasses().title} variant="h6" color="inherit" noWrap={true}>
                            FCMS
                        </Typography>
                        {this.props.searchItems &&
                        <div className={getClasses().search}>
                            <div className={getClasses().searchIcon}>
                                <SearchIcon/>
                            </div>
                            <Select
                                filterOption={createFilter({ignoreAccents: false})}
                                components={{MenuList}}
                                className={"react-select"}
                                classNamePrefix="react-select"
                                placeholder={"Search record name..."}
                                value={this.state.selectedOption}
                                onChange={this.handleChange}
                                options={this.props.searchItems}
                                inputValue={this.state.inputValue}
                                onInputChange={this.onInputChange}
                                menuIsOpen={this.state.menuOpen}
                                onMenuOpen={this.onMenuOpen}
                                onMenuClose={this.onMenuClose}
                            />
                        </div>
                        }
                        <div className={getClasses().grow}/>
                        <div className={getClasses().sectionDesktop}>
                            {this.props.status && <span>{this.props.status}</span>}
                            <IconButton color={"inherit"}>
                                <Link to={"/code"} className={getClasses().linkWrapper}>
                                    <CallbackIcon/>
                                </Link>
                            </IconButton>
                            <IconButton color={"inherit"}>
                                <Link to={"/history"} className={getClasses().linkWrapper}>
                                    {notificationIcon}
                                </Link>
                            </IconButton>
                            <IconButton color={"inherit"} onClick={this.logout}>
                                <LogoutIcon/>
                            </IconButton>
                        </div>
                        <div className={getClasses().sectionMobile}>
                            <IconButton aria-haspopup="true" onClick={this.handleMobileMenuOpen} color="inherit">
                                <MoreIcon/>
                            </IconButton>
                        </div>
                    </Toolbar>
                </AppBar>
                {renderMobileMenu}
            </div>
        );
    }

    private logout = () => {
        this.props.onLogout();
    };

    private handleChange = (selectedOption: SearchItem) => {
        this.setState({selectedOption});
        const ids = selectedOption.value;
        if (ids === undefined) {
            return;
        }
        this.chainFocus(ids, 0)
    };

    private chainFocus(ids: string[], index: number, searchDuration = 0) {
        if (index < ids.length && searchDuration < 1000) {
            const element = document.getElementById(ids[index]);
            if (element === null) {
                const delay = 10;
                setTimeout(() => {
                    this.chainFocus(ids, index, searchDuration + delay);
                }, delay)
            } else {
                element.scrollIntoView();
                element.focus();
                setTimeout(() => {
                    this.chainFocus(ids, index + 1);
                });
            }
        }
    }

    private onInputChange = (inputValue: string) => {
        cache.clearAll();
        this.setState({inputValue});
    };
    private onMenuOpen = () => {
        this.setState({menuOpen: true});
    };
    private onMenuClose = () => {
        this.setState({menuOpen: false});
    };
}

interface IAppBarProps extends AppBarProps {
    searchItems?: SearchItem[];
    unreadCount: number;
    status?: string;

    onLogout(): void;
}

interface IAppBarState {
    mobileMoreAnchorEl: any;
    selectedOption?: any;
    inputValue: string;
    menuOpen: boolean;
}

export default withStyles(styles)(PrimarySearchAppBar);